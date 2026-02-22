import { 
  ChangeDetectionStrategy, Component, computed, effect, inject, OnDestroy, OnInit 
} from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product, variantResponse } from '../../../core/models/product';
import { CommonModule } from '@angular/common';
import { Subject, fromEvent, interval, merge } from 'rxjs';
import { filter, map, startWith, switchMap, takeUntil, tap, withLatestFrom } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { ProductService } from '../../../core/services/product.service';
import { RecommendationService } from '../../../core/services/recommendation.service';
import { SwalService } from '../../../core/services/swal-service.service';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../../environments/environment';
import { ConfigGlobalService } from '../../../core/services/config-global.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, CurrencyFormatPipe,TranslateModule],
  templateUrl: './cart.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [style({ opacity: 0 }), animate('300ms ease-out', style({ opacity: 1 }))]),
      transition(':leave', [animate('200ms ease-in', style({ opacity: 0 }))])
    ]),
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(100%)' }),
        animate('300ms cubic-bezier(0.4, 0, 0.2, 1)', style({ transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('250ms cubic-bezier(0.4, 0, 0.2, 1)', style({ transform: 'translateX(100%)' }))
      ])
    ]),
    trigger('fadeInUp', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate('300ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('200ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 0, transform: 'translateY(20px)' }))
      ])
    ])
  ]
})
export class CartComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  private dialog = inject(MatDialog);
  private router = inject(Router);
  private globalConfig = inject(ConfigGlobalService);
  configGlobalSignal = this.globalConfig.configGlobal;
  private destroy$ = new Subject<void>();
  imagesBaseUrl=environment.imageUrlBase;
  // Signals
  authState = this.authService.authState;
  isCartOpen = this.cartService.isOpen;
  addReduceButtonLoading = false;
  isCartEmpty = computed(() => this.authState().isAuthenticated ? this.cartService.isEmpty() : this.cartService.isGuestCartEmpty());
  isLoading=this.cartService.isLoading;
  recommendedProducts=this.productService.recommendations;
  // Computed values

  subtotal = this.cartService.subtotal;
  shipping = this.cartService.shipping;
  total = this.cartService.total;

  constructor(private productService: ProductService,
    private recommendationService: RecommendationService,
    private swalService: SwalService
  ) {}

 ngOnInit(): void {

  
  // Load the appropriate cart immediately
  if (this.authState().isAuthenticated) {
    
    this.loadUserCart();
    
    // Set up periodic sync only after initial load
    interval(60000)
      .pipe(
        takeUntil(this.destroy$),
        withLatestFrom(
          merge(
            fromEvent(document, 'visibilitychange').pipe(
              map(() => document.visibilityState === 'visible')
            ),
            interval(1000).pipe(map(() => document.visibilityState === 'visible'))
          ).pipe(startWith(document.visibilityState === 'visible'))
        ),
        filter(([, visible]) => visible),
        switchMap(() => this.cartService.createAndGetCart(this.authService.getUserId!))
      )
      .subscribe({
        error: (err) => console.error('Failed to sync cart', err)
      });
  } else {
    this.loadGuestCart();
  }
  //this.loadRecommandedProducts();
}

  showProductDetails(productId: number): void {
    this.router.navigate(['/product', productId]);
    this.cartService.closeCart()
  }

private loadUserCart(): void {
  if (!this.authService.getUserId) return;  
  
  this.cartService.createAndGetCart(this.authService.getUserId).subscribe();
}
  activeCart = computed(() => {
    return this.authState().isAuthenticated 
      ? this.cartService.cart() 
      : this.cartService.guestCart();
  });

  private loadGuestCart(): void {
    const guestCart = localStorage.getItem('guest_cart');
    this.cartService.updateGuestCart(guestCart ? JSON.parse(guestCart) : { id: 0, articles: [] });    
  }
  
// loadRecommandedProducts(): void {
//   const isAuth = this.authService.authState().isAuthenticated;
//   const userId = isAuth ? this.authService.authState().id : null;
//   const sessionToken = isAuth ? '' : localStorage.getItem('anonyme-session-token');

//   this.recommendationService.recommend(sessionToken, userId).subscribe({
//     next: (response) => {      
//       if (response.recommended_products.length === 0) {
//         return;
//       }      
//       const productIds = response.recommended_products.map((p: any) => p.id);
//       this.productService.getProductsByIds(productIds).subscribe({
//         next: (products) => {
//           this.productService.updateRecommendedProducts(products);
//         },
//         error: (error) => {
//           console.error(error);
//         }
//       });
//     },
//     error: (error) => {
//       console.error(error);

//     }
//   });
// }
  incrementQuantity(product: Product, variant: variantResponse): void {
    if (variant.availableQuantity! <= 0) {
      this.swalService.warning('SWAL.INSUFFICIENT_STOCK');
      return;
    }

    try {
      if (this.authService.isAuthenticated) {
        this.cartService.addItemToCart(product, variant, 1, this.authService.getUserId!);
      } else {
        this.cartService.addItemToGuestCart(product, variant, 1);
      }
      this.swalService.success('SWAL.CART.ITEM_ADDED_CART');
    } catch (err) {
      this.swalService.error((err as Error).message || 'SWAL.CART.ADD_CART_FAILED');
    }
  }

  decrementQuantity(product: Product, variant: variantResponse): void {
    this.addReduceButtonLoading = true;
    try {
      if (this.authService.isAuthenticated) {
        this.cartService.decreaseOrRemoveItemFromCart(product, variant, 1, this.authService.getUserId!).subscribe();
      } else {
        this.cartService.decreaseOrRemoveItemFromGuestCart(product, variant, 1);
      }
      this.swalService.success('SWAL.CART.ITEM_QUANTITY_UPDATED');
    } catch (err) {
      this.swalService.error((err as Error).message || 'SWAL.CART.FAILED_UPDATE');
    } finally {
      this.addReduceButtonLoading = false;
    }
  }
 removeProduct(product: Product, variant: variantResponse): void {
    try {
      if (this.authService.isAuthenticated) {
        this.cartService.removeVariantFromCart(product, variant, this.authService.getUserId!).subscribe();
      } else {
        this.cartService.removeItemFromGuestCart(product, variant);
      }
      this.swalService.success('SWAL.CART.ITEM_REMOVED_CART');
    } catch (err) {
      this.swalService.error((err as Error).message || 'SWAL.CART.FAILED_UPDATE');
    }
  }

  clearCart(): void {
    try {
      if (this.authService.isAuthenticated) {
        this.cartService.removeAllItemsFromCart(this.authService.getUserId!).subscribe();
      } else {
        this.cartService.removeAllItemsFromGuestCart();
      }
      this.swalService.success('SWAL.CART.CLEARED');
    } catch (err) {
      this.swalService.error('SWAL.CART.FAILED_UPDATE');
    }
  }

  proceedToCheckout(): void {
          this.closeCart();
          this.router.navigate(['/checkout']);
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getProductPrice(product: Product): number {
    return product.newPrice || product.prix;
  }

  getProductImage(product: Product): string {
    return product.imageProduits[0]?.url || 'assets/images/placeholder-product.png';
  }

  closeCart(): void {
    this.cartService.closeCart();
  }
}