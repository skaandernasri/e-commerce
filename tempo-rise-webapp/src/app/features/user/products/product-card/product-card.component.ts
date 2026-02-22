import { Component, Input, inject, OnDestroy, computed, OnInit } from '@angular/core';
import { Product } from '../../../../core/models/product';
import { CommonModule } from '@angular/common';
import { RatingComponent } from '../rating/rating.component';
import { Promotion } from '../../../../core/models/promotion';
import { CartService } from '../../../../core/services/cart.service';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject} from 'rxjs';
import { CurrencyFormatPipe } from '../../../../core/pipe/currency-pipe.pipe';
import { ProductService } from '../../../../core/services/product.service';
import { Router } from '@angular/router';
import { ExpandLongTextComponent } from "../../../../shared/components/expand-long-text/expand-long-text.component";
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../../../environments/environment';
@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RatingComponent, MatSnackBarModule, CurrencyFormatPipe, ExpandLongTextComponent,
    TranslateModule
  ],
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.css']
})
export class ProductCardComponent implements OnInit, OnDestroy {
  private cartService = inject(CartService);
  private productService = inject(ProductService);
  products = this.productService.products;
  imageBaseUrl = environment.imageUrlBase;
  @Input() product!: Product;
  // @Input() currentImageIndex = 0;
  currentImageIndex: { [productId: number]: number } = {};
  isImageChanging: { [productId: number]: boolean } = {};
  // @Input() isImageChanging = false;
  // @Input() rating = 0;
  // @Input() reviewCount = 0;
  // @Input() discountedPrice = 0;
  // @Input() hasActivePromo = false;
  // @Input() currentPromotion?: Promotion;
  // @Output() imageChange = new EventEmitter<number>();
  // @Output() viewDetails = new EventEmitter<number>();

  private destroy$ = new Subject<void>();
  isLoading = this.cartService.isLoading;
  constructor(private router: Router){}
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  ngOnInit(): void {    
    this.currentImageIndex[this.product.id] = 0;
    this.isImageChanging[this.product.id] = false;
  }
  stripHtml(html: string): string {
    const div = document.createElement('div');
    div.innerHTML = html;
    return div.textContent || div.innerText || '';
  }
    showProductDetails(productId: number): void {
    this.router.navigate(['/product', productId]);
  }
productRatings(){
  let ratings: number = 0;
  if (this.product.avis?.length) {
    const total = this.product.avis.reduce((sum, avis) => sum + (avis?.note || 0), 0);
    ratings = total / this.product.avis.length;
  }
  return ratings;
}
productReviewCounts(){
  let counts: number = 0;
  if (this.product.avis) {
    counts = this.product.avis.length;
  }
  return counts;
}
  onImageChange(productId: number, index: number): void {
      this.isImageChanging[productId] = true;
      this.currentImageIndex[productId] = index;
      this.isImageChanging[productId] = false;
  }
   onViewDetails(productId: number): void {
    this.router.navigate(['/product', productId]);
  }
  currentPromotion(product: Product): Promotion | undefined {
      return this.productService.getCurrentPromotion(product);
    }
    hasActivePromo(product: Product): boolean {
    return this.productService.hasActivePromotion(product);
  }
}