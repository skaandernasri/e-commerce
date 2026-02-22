import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { WhishlistDto, WhishlistService } from '../../../core/services/whishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { ProductCardComponent } from "../products/product-card/product-card.component";
import { Product } from '../../../core/models/product';
import { ProductService } from '../../../core/services/product.service';
import { Promotion } from '../../../core/models/promotion';

@Component({
  selector: 'app-whishlist',
  imports: [ProductCardComponent],
  templateUrl: './whishlist.component.html',
  styleUrl: './whishlist.component.css'
})
export class WhishlistComponent implements OnInit {
  private authService = inject(AuthService);
  private whishlistService = inject(WhishlistService);
  
  wishlistProducts = this.whishlistService.wishlist;
  currentImageIndex: { [productId: number]: number } = {};
  isImageChanging: { [productId: number]: boolean } = {};
  isLoading = this.whishlistService.isLoading$;

  // Use signals for ratings and counts to prevent ExpressionChangedAfterItHasBeenCheckedError
  private _productRatings = signal<Record<number, number>>({});
  private _productReviewCounts = signal<Record<number, number>>({});

  // Computed values that won't cause change detection issues
  productRatings = computed(() => this._productRatings());
  productReviewCounts = computed(() => this._productReviewCounts());

  constructor(
    private router: Router,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist() {
    this.whishlistService.getWhishlistByUserId(this.authService.getUserId!).subscribe({
      next: () => {
        this.calculateProductMetrics();
      },
      error: (error) => {
        console.error('Error fetching whishlist:', error);
      }
    });
  }

  // Calculate both ratings and counts at once
  private calculateProductMetrics(): void {
    const ratings: Record<number, number> = {};
    const counts: Record<number, number> = {};

    if (!this.wishlistProducts()?.produits) {
      this._productRatings.set(ratings);
      this._productReviewCounts.set(counts);
      return;
    }

    this.wishlistProducts().produits.forEach(product => {
      // Initialize ratings
      ratings[product.id] = 0;
      if (product.avis?.length) {
        const total = product.avis.reduce((sum, avis) => sum + (avis?.note || 0), 0);
        ratings[product.id] = total / product.avis.length;
      }

      // Initialize counts
      counts[product.id] = product.avis?.length || 0;

      // Initialize image index if not exists
      if (this.currentImageIndex[product.id] === undefined) {
        this.currentImageIndex[product.id] = 0;
      }
    });

    // Update signals once with all calculated values
    this._productRatings.set(ratings);
    this._productReviewCounts.set(counts);
  }

  removeAllFromWishlist() {
    if (confirm(`Êtes-vous sûr de vouloir supprimer tous les ${this.wishlistProducts()?.produits?.length} produits de votre liste de souhaits ?`)) {
      this.whishlistService.removeAllItemsFromWhishlist(this.authService.getUserId!).subscribe({
        error: (error) => {
          console.error('Error removing all items from wishlist:', error);
        }
      });
    }
  }

  removeFromWishlist(productId: number) {
    if (confirm('Êtes-vous sûr de vouloir retirer ce produit de votre liste de souhaits ?')) {
      this.whishlistService.removeItemFromWhishlist(this.authService.getUserId!, productId).subscribe({
        next: () => {
          // Recalculate metrics after removal
          this.calculateProductMetrics();
        },
        error: (error) => {
          console.error('Error removing item from whishlist:', error);
        }
      });
    }
  }

  onImageChange(productId: number, index: number): void {
    this.isImageChanging[productId] = true;
    this.currentImageIndex[productId] = index;
    setTimeout(() => {
      this.isImageChanging[productId] = false;
    }, 0);
  }

  continueShopping() {
    this.router.navigate(['/products']);
  }

  viewProductDetails(productId: number): void {
    this.router.navigate(['/product', productId]);
  }

  currentPromotion(product: Product): Promotion | undefined {
    return this.productService.getCurrentPromotion(product);
  }

  hasActivePromo(product: Product): boolean {
    return this.productService.hasActivePromotion(product);
  }
}