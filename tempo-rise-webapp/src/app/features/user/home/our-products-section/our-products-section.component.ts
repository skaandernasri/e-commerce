import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ProductService } from '../../../../core/services/product.service';
import { ParamsService } from '../../../../core/services/params.service';
import { environment } from '../../../../../environments/environment';
import { ProductCardComponent } from '../../products/product-card/product-card.component';
import { TranslateModule } from '@ngx-translate/core';
import { RouterModule } from '@angular/router';
import { ParamSectionResponse } from '../../../../core/models/params';
interface OurProductsContent{
  title: string;
  buttonText: string;
  buttonLink: string;
}
@Component({
  selector: 'app-our-products',
  imports: [ProductCardComponent,TranslateModule,RouterModule],
  templateUrl: './our-products-section.component.html',
  styleUrl: './our-products-section.component.css'
})
export class OurProductsSectionComponent implements OnInit {
@Input() activeOurProductsSection!: Signal<ParamSectionResponse | undefined>
  private productService = inject(ProductService);
imageBaseUrl = environment.imageUrlBase;
isLoadingProducts = this.productService.isLoading;
products = this.productService.homeProducts;
  ourProductsContent:Signal<OurProductsContent | null> =computed(() => {
    const content = this.activeOurProductsSection()!.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title,
        buttonText: parsed.buttonText,
        buttonLink: parsed.buttonLink
      };
    }
    return null;
  })
  loadInitialProducts(): void {
    const params = {
      page: 0,
      size: 3,
      actif: true,
      orderByRatingDesc: true
    };
    this.productService.loadHomeProducts(params);

  };
  ngOnInit(): void {
    this.loadInitialProducts();
  }
}
