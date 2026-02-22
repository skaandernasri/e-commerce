import {
  Component,
  OnInit,
  OnDestroy,
  inject,
  effect,
  computed,
  signal,
  Signal,
  WritableSignal,
  untracked
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, finalize, catchError } from 'rxjs/operators';

import { Product } from '../../../../core/models/product';
import { ProductService } from '../../../../core/services/product.service';
import { AvisService } from '../../../../core/services/avis.service';
import { AuthService } from '../../../../core/services/auth.service';
import { CartService } from '../../../../core/services/cart.service';
import { RatingComponent } from '../rating/rating.component';
import { ExpandLongTextComponent } from '../../../../shared/components/expand-long-text/expand-long-text.component';
import { Avis, avisRequest } from '../../../../core/models/avis';
import { RecommendationService } from '../../../../core/services/recommendation.service';
import { CurrencyFormatPipe } from '../../../../core/pipe/currency-pipe.pipe';
import { WhishlistService } from '../../../../core/services/whishlist.service';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { TranslateModule } from '@ngx-translate/core';
import { RecommandedProductsComponent } from '../../recommanded-products/recommanded-products.component';
import { SwalService } from '../../../../core/services/swal-service.service';
import { environment } from '../../../../../environments/environment';
import { ParamsService } from '../../../../core/services/params.service';
import {  QuillViewHTMLComponent } from "ngx-quill";
import { HeroSectionComponent } from '../../home/hero-section/hero-section.component';
import { FeaturesSectionComponent } from "../../home/features-section/features-section.component";
import { MetaPixelService } from '../../../../core/services/meta-pixel.service';

@Component({
  selector: 'app-product-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RatingComponent,
    ExpandLongTextComponent,
    CurrencyFormatPipe,
    PaginationComponent,
    TranslateModule,
    RecommandedProductsComponent,
    QuillViewHTMLComponent],
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.css']
})
export class ProductDetailsComponent implements OnInit, OnDestroy {
  // ──────── Services ────────
  private readonly authService = inject(AuthService);
  private readonly productService = inject(ProductService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cartService = inject(CartService);
  private readonly avisService = inject(AvisService);
  private readonly whishlistService = inject(WhishlistService);
  private readonly swalService = inject(SwalService);
  private readonly paramService = inject(ParamsService);
  private readonly metaPixelService = inject(MetaPixelService);
  private readonly destroy$ = new Subject<void>();

  // ──────── Product State ────────
  readonly product= this.productService.productDetails;
  readonly selectedSize = signal<string>('');
  readonly selectedColor = signal<string>('');
  readonly quantity = signal<number>(1);
  readonly recommendedProducts = this.productService.recommendations;
  activeProductDetailsSections= computed(() => this.paramService.productDetailsPageSections().filter(section => section.active));
  activeProductDetailsMainSections= computed(() => this.activeProductDetailsSections().find(section => section.type === 'MAIN'));
  activeProductDetailsDescriptionSections= computed(() => this.activeProductDetailsSections().find(section => section.type === 'DESCRIPTION'));
  activeProductDetailsReviewsSections= computed(() => this.activeProductDetailsSections().find(section => section.type === 'OUR_REVIEWS'));
  activeProductDetailsContent1Sections= computed(() => this.activeProductDetailsSections().find(section => section.type === 'CONTENT1'));
  currentImageIndex = 0;
  isImageChanging = false;
  addToCartButtonIsLoading = false;

  // ──────── User & Auth ────────
  readonly isLoggedIn = this.authService.isAuthenticated;
  readonly userId = this.authService.getUserId;
  readonly cart = computed(() => 
    this.isLoggedIn ? this.cartService.getCart : this.cartService.getGuestCart
  );

  // ──────── Reviews ────────
  readonly allReviews: WritableSignal<Avis[]> = signal([]);
  readonly pagination = signal<{ page: number; size: number }>({ page: 1, size: 10 });
  totalElements = 0;
  isLoadingReviews = false;
  showReviewForm = false;
  newReview: avisRequest = this.createEmptyReview();

  // ──────── UI State ────────
  readonly tabs = [
    { id: 'description', title: 'Description' },
    { id: 'composition', title: 'Composition' },
    { id: 'guide', title: "Guide d'entretien" },
    { id: 'faq', title: 'FAQ' }
  ] as const;
  
  activeTab: string = 'description';


  readonly imagesBaseUrl = environment.imageUrlBase;

  // ──────── Computed Values ────────
  readonly selectedVariant: Signal<Product['variants'][0] | undefined> = computed(() => {
    const prod = this.product();
    if (!prod) return undefined;
    
    return prod.variants.find(
      v => v.size === this.selectedSize() && v.color === this.selectedColor()
    );
  });

  readonly uniqueSizes = computed(() => {
    const variants = this.product()?.variants ?? [];
    return [...new Set(variants.map(v => v.size).sort((a, b) => +a - +b))];
  });

  readonly uniqueColors = computed(() => {
    const variants = this.product()?.variants ?? [];
    return [...new Set(variants.map(v => v.color))];
  });

  readonly discountedPrice = computed(() => {
    const prod = this.product();
    return prod ? this.productService.calculateDiscountedPrice(prod) : 0;
  });

  readonly hasActivePromo = computed(() => {
    const prod = this.product();
    return prod ? this.productService.hasActivePromotion(prod) : false;
  });

  readonly currentPromotion = computed(() => {
    const prod = this.product();
    return prod ? this.productService.getCurrentPromotion(prod) : undefined;
  });

  readonly averageRating = computed(() => {
    const avis = this.allReviews();
    if (!avis.length) return 0;
    const total = avis.reduce((sum, a) => sum + (a?.note || 0), 0);
    return total / avis.length;
  });

  readonly availableSizesForSelectedColor = computed(() => {
    const color = this.selectedColor();
    if (!color) return [];
    
    return (this.product()?.variants ?? [])
      .filter(v => v.color === color && (v.availableQuantity ?? 0) > 0)
      .map(v => v.size);
  });

  readonly availableColorsForSelectedSize = computed(() => {
    const size = this.selectedSize();
    if (!size) return [];
    
    return (this.product()?.variants ?? [])
      .filter(v => v.size === size && (v.availableQuantity ?? 0) > 0)
      .map(v => v.color);
  });

  constructor() {
    // Reload reviews when pagination changes
    effect(() => {
      const p = this.pagination();
      const prodId = this.product()?.id;
      const product = this.product();      
      // Use untracked to avoid creating dependency on product in effect
      untracked(() => {
        if (prodId) {
          this.loadReviews();
        }
      });
    });
  }

  ngOnInit(): void {
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        const id = Number(this.route.snapshot.params['id']);
        if (!isNaN(id) && id > 0) {
          this.loadProduct(id);
        }
      });
      this.paramService.getSectionsByPageType('PRODUCT_DETAILS').subscribe();
    //this.loadRecommendedProducts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  } 

  // ──────── Product Loading ────────
private loadProduct(id: number): void {
  this.productService
    .getProductById(id)
    .pipe(
      takeUntil(this.destroy$),
      catchError(error => {
        console.error('Error loading product:', error);
        this.swalService.error('SWAL.PRODUCT_LOAD_FAILED');
        throw error;
      })
    )
    .subscribe(product => {
      //this.product.set(product);
      const quantity = product.variants.map(v => v.quantity).reduce((a, b) => a + b, 0);
      this.metaPixelService.addViewContentEvent([product.id.toString()], 'product', [{id: product.id.toString(), quantity: quantity }],"TND", product.newPrice);
      this.resetSelections();
      this.selectVariant(product);
      // Clear caches when product changes  
      this.sizeAvailabilityCache.clear();
      this.colorAvailabilityCache.clear();
      this.loadReviews();
    });
}

  // private loadRecommendedProducts(): void {
  //   const authState = this.authService.authState();
  //   const userId = authState.isAuthenticated ? authState.id : null;
  //   const sessionToken = authState.isAuthenticated 
  //     ? '' 
  //     : localStorage.getItem('anonyme-session-token');

  //   this.recommendationService
  //     .recommend(sessionToken, userId)
  //     .pipe(takeUntil(this.destroy$))
  //     .subscribe({
  //       next: response => {
  //         if (!response?.recommended_products?.length) return;
          
  //         const productIds = response.recommended_products.map((p: any) => p.id);
  //         this.productService
  //           .getProductsByIds(productIds)
  //           .pipe(takeUntil(this.destroy$))
  //           .subscribe({
  //             next: products => this.productService.updateRecommendedProducts(products),
  //             error: error => console.error('Error loading recommended products:', error)
  //           });
  //       },
  //       error: error => console.error('Error fetching recommendations:', error)
  //     });
  // }

  // ──────── Reviews ────────
  loadReviews(): void {
    const prod = this.product();
    if (!prod || this.isLoadingReviews) return;

    const { page, size } = this.pagination();
    this.isLoadingReviews = true;

    this.avisService
      .getAvisByProductIdPaged(prod.id, page, size)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoadingReviews = false))
      )
      .subscribe({
        next: response => {
          this.totalElements = response.totalElements;
          this.allReviews.set(response.content || []);
        },
        error: error => {
          console.error('Error loading reviews:', error);
          this.swalService.error('SWAL.REVIEWS_LOAD_FAILED');
        }
      });
  }

 submitReview(): void {
  const uid = this.userId;
  if (!uid) {
    this.swalService.warning('SWAL.LOGIN_REQUIRED');
    return;
  }

  const prod = this.product();
  if (!prod?.id || !this.newReview.note) {
    this.swalService.warning('SWAL.REVIEW_INCOMPLETE');
    return;
  }

  this.avisService
    .createAvis({
      ...this.newReview,
      produitId: prod.id,
      utilisateurId: uid
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: () => {
        // Reset form first
        this.resetReviewForm();
        // Then reset to first page - the effect will trigger loadReviews
        this.pagination.set({ page: 1, size: this.pagination().size });
        this.swalService.success('SWAL.REVIEW_SUBMITTED');
      },
      error: error => {
        console.error('Failed to submit review', error);
        this.swalService.error('SWAL.REVIEW_SUBMIT_FAILED');
      }
    });
}
  onPageChange(page: number): void {
    this.pagination.update(p => ({ ...p, page }));
  }

  // ──────── Cart Actions ────────
  addToCart(): void {
    const prod = this.product();
    if (!prod) return;

    const variant = this.selectedVariant();
    if (!variant) {
      this.swalService.warning('SWAL.SELECT_SIZE_COLOR');
      return;
    }

    this.addToCartButtonIsLoading = true;

    try {
      const uid = this.userId;
      if (uid) {
        this.cartService.addItemToCart(prod, variant, this.quantity(), uid);
      } else {
        this.cartService.addItemToGuestCart(prod, variant, this.quantity());
      }

      this.swalService.success('SWAL.CART.ITEM_ADDED_CART');
      this.resetSelections();
    } catch (err) {
      console.error('Failed to add item to cart:', err);
      this.swalService.error('SWAL.CART.ADD_CART_FAILED');
    } finally {
      this.addToCartButtonIsLoading = false;
    }
  }

  incrementQuantity(): void {
    const variant = this.selectedVariant();
    if (!variant) return;

    const available = variant.availableQuantity ?? 0;
    if (this.quantity() < available) {
      this.quantity.update(q => q + 1);
    }
  }

  decrementQuantity(): void {
    if (this.quantity() > 1) {
      this.quantity.update(q => q - 1);
    }
  }

  // ──────── Wishlist Actions ────────
  addItemToWishlist(): void {
    const prod = this.product();
    if (!prod) return;

    if (!this.isLoggedIn) {
      this.swalService.warningWithAction(
        'SWAL.LOGIN_REQUIRED_WISHLIST',
        () => this.router.navigate(['/auth/user/signin'])
      );
      return;
    }

    const uid = this.userId;
    if (!uid) return;

    this.whishlistService
      .addItemToWhishlist(uid, prod.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.swalService.success('SWAL.WISHLIST.WISHLIST_ADDED');
        },
        error: error => {
          if (error.error?.code === '4020') {
            this.swalService.error('SWAL.WISHLIST.ALREADY_IN_WISHLIST');
          } else {
            this.swalService.error('SWAL.WISHLIST.WISHLIST_ADD_FAILED');
            console.error('Wishlist error:', error);
          }
        }
      });
  }

  // ──────── UI Helpers ────────
  selectTab(tabId: string): void {
    this.activeTab = tabId;
  }

  clickOnColor(color: string): void {
    this.selectedColor.update(current => (current === color ? '' : color));
    this.quantity.set(1);
  }

  clickOnSize(size: string): void {
    this.selectedSize.update(current => (current === size ? '' : size));
    this.quantity.set(1);
  }

  changeImage(index: number): void {
    if (index === this.currentImageIndex || this.isImageChanging) return;

    this.isImageChanging = true;
    setTimeout(() => {
      this.currentImageIndex = index;
      this.isImageChanging = false;
    }, 150);
  }

  showProductDetails(productId: number): void {
    this.router.navigate(['/product', productId]);
  }

  closeCart(): void {
    this.cartService.closeCart();
  }

  // ──────── Availability Checks ────────
  private readonly sizeAvailabilityCache = new Map<string, Signal<boolean>>();
  private readonly colorAvailabilityCache = new Map<string, Signal<boolean>>();

  isSizeUnavailable(size: string): boolean {
  if (!this.sizeAvailabilityCache.has(size)) {
    this.sizeAvailabilityCache.set(size, this.sizeNotAvailableSignal(size));
  }
  return this.sizeAvailabilityCache.get(size)!();
}

  isColorUnavailable(color: string): boolean {
  if (!this.colorAvailabilityCache.has(color)) {
    this.colorAvailabilityCache.set(color, this.colorNotAvailableSignal(color));
  }
  return this.colorAvailabilityCache.get(color)!();
}

private sizeNotAvailableSignal(size: string): Signal<boolean> {
  return computed(() => {
    const prod = this.product();
    if (!prod) return false;

    const variantsForSize = prod.variants.filter(v => v.size === size);
    if (!variantsForSize.length) return false;

    if (this.isLoggedIn) {
      // For logged-in users, check cart items
      const cartItem = this.cart().articles.find(p => p.id === prod.id);
      
      const allOutOfStock = variantsForSize.every(
        v => (v.availableQuantity ?? 0) === 0
      );

      const allReachedCartQuantity = variantsForSize.every(v => {
        const cartVariant = cartItem?.variants.find(v1 => v1.id === v.id);
        const available = v.availableQuantity ?? 0;
        const inCart = cartVariant?.cartQuantity ?? 0;
        return available === inCart;
      });

      return allOutOfStock || allReachedCartQuantity;
    }

    // Guest user logic remains the same
    const guestCartItem = this.cartService.getGuestCart.articles.find(
      p => p.id === prod.id
    );

    const allOutOfStock = variantsForSize.every(
      v => Math.max((v.availableQuantity ?? 0) - (v.reservedQuantity ?? 0),0) === 0
    );

    const allReachedCartQuantity = variantsForSize.every(v => {
      const cartVariant = guestCartItem?.variants.find(v1 => v1.id === v.id);
      const remaining = (v.availableQuantity ?? 0) - (v.reservedQuantity ?? 0);
      const inCart = cartVariant?.cartQuantity ?? 0;
      return remaining === inCart;
    });

    return allOutOfStock || allReachedCartQuantity;
  });
}

  private colorNotAvailableSignal(color: string): Signal<boolean> {
  return computed(() => {
    const prod = this.product();
    if (!prod) return false;

    const variantsForColor = prod.variants.filter(v => v.color === color);
    if (!variantsForColor.length) return false;

    if (this.isLoggedIn) {
      // For logged-in users, check cart items
      const cartItem = this.cart().articles.find(p => p.id === prod.id);
      
      const allOutOfStock = variantsForColor.every(
        v => (v.availableQuantity ?? 0) === 0
      );

      const allReachedCartQuantity = variantsForColor.every(v => {
        const cartVariant = cartItem?.variants.find(v1 => v1.id === v.id);
        const available = v.availableQuantity ?? 0;
        const inCart = cartVariant?.cartQuantity ?? 0;
        return available === inCart;
      });

      return allOutOfStock || allReachedCartQuantity;
    }

    // Guest user logic remains the same
    const guestCartItem = this.cartService.getGuestCart.articles.find(
      p => p.id === prod.id
    );

    const allOutOfStock = variantsForColor.every(
      v => Math.max((v.availableQuantity ?? 0) - (v.reservedQuantity ?? 0),0) === 0
    );

    const allReachedCartQuantity = variantsForColor.every(v => {
      const cartVariant = guestCartItem?.variants.find(v1 => v1.id === v.id);
      const remaining = (v.availableQuantity ?? 0) - (v.reservedQuantity ?? 0);
      const inCart = cartVariant?.cartQuantity ?? 0;
      return remaining === inCart;
    });

    return allOutOfStock || allReachedCartQuantity;
  });
}

  // ──────── Star Rating Helpers ────────
  getStarWidth(rating: number, starPosition: number): number {
    if (rating >= starPosition) return 100;
    if (rating > starPosition - 1) {
      return Math.round((rating - (starPosition - 1)) * 100);
    }
    return 0;
  }

  getStarCount(star: number): number {
    return this.allReviews().filter(r => Math.round(r.note) === star).length;
  }

  getStarPercentage(star: number): number {
    const total = this.allReviews().length;
    return total ? (this.getStarCount(star) / total) * 100 : 0;
  }

  // ──────── Helper Methods for External Products ────────
  uniqueColorsForProd(product: Product): string[] {
    return [...new Set(product.variants.map(v => v.color))];
  }

  uniqueSizesForProd(product: Product): string[] {
    return [...new Set(product.variants.map(v => v.size).sort( (a, b) => +a - +b))];
  }

  // ──────── Private Helpers ────────
  private resetSelections(): void {
    this.selectedColor.set('');
    this.selectedSize.set('');
    this.quantity.set(1);
  }

  private resetReviewForm(): void {
    this.newReview = this.createEmptyReview();
    this.showReviewForm = false;
  }

  private createEmptyReview(): avisRequest {
    return {
      note: 0,
      commentaire: '',
      datePublication: '',
      utilisateurId: this.userId,
      produitId: this.product()?.id || null
    };
  }
  selectVariant(product:Product): void {
    if(this.uniqueSizesForProd(product).length === 0) return;
    if(this.uniqueColorsForProd(product).length === 0) return;
    for(let size of this.uniqueSizesForProd(product)){
      this.selectedSize.set(size);
      for(let color of this.uniqueColorsForProd(product)){
        if(this.availableColorsForSelectedSize().includes(color) ){
          this.selectedColor.set(color);
          return;
        }
      } 
    }
  }
  colorIsActive(color: string): boolean {
    return this.selectedColor() === color && !this.isColorUnavailable(color) && (!this.selectedSize() || this.availableColorsForSelectedSize().includes(color))
  }
  colorIsInActiveSize(color: string): boolean {
    return this.isColorUnavailable(color) || (this.selectedSize()!== '' && !this.availableColorsForSelectedSize().includes(color))
  }
  sizeIsActive(size: string): boolean {
    return this.selectedSize() === size && !this.isSizeUnavailable(size) && (!this.selectedColor() || this.availableSizesForSelectedColor().includes(size))
  }
  sizeIsInActiveColor(size: string): boolean {
    return this.isSizeUnavailable(size) || (this.selectedColor()!== '' && !this.availableSizesForSelectedColor().includes(size))
  }
}