import { Component, inject, OnInit } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { AuthService } from '../../../core/services/auth.service';
import { RecommendationService } from '../../../core/services/recommendation.service';
import { Subject, takeUntil } from 'rxjs';
import { Router } from '@angular/router';
import { Product } from '../../../core/models/product';
import { TranslateModule } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProductCardComponent } from "../products/product-card/product-card.component";

@Component({
  selector: 'app-recommanded-products',
  imports: [CommonModule,
    FormsModule,
    TranslateModule, ProductCardComponent],
  templateUrl: './recommanded-products.component.html',
  styleUrl: './recommanded-products.component.css'
})
export class RecommandedProductsComponent implements OnInit{
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private destroy$ = new Subject<void>();
  recommendedProducts = this.productService.recommendations;
  constructor(private recommendationService: RecommendationService,
    private router: Router
  ) { }
  ngOnInit(): void {
    //this.loadRecommandedProducts();
  }
  loadRecommandedProducts(): void {
    const isAuth = this.authService.authState().isAuthenticated;
    const userId = isAuth ? this.authService.authState().id : null;
    const sessionToken = isAuth ? '' : localStorage.getItem('anonyme-session-token');

    this.recommendationService.recommend(sessionToken, userId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        if (!response?.recommended_products?.length) return;
        const productIds = response.recommended_products.map((p: any) => p.id);
        this.productService.getProductsByIds(productIds).pipe(takeUntil(this.destroy$)).subscribe({
          next: (products) => this.productService.updateRecommendedProducts(products),
          error: (error) => console.error(error)
        });
      },
      error: (error) => console.error(error)
    });
  }
   showProductDetails(productId: number): void {
    this.router.navigate(['/product', productId]);
  }
    uniqueColorsForProd(product: Product): string[] {
      return [...new Set(product.variants.map(v => v.color))];
    }
    uniqueSizesForProd(product: Product): string[] {
      return [...new Set(product.variants.map(v => v.size))];
    }
}
