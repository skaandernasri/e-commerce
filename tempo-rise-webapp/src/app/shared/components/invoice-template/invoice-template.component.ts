import { Component, ElementRef, inject, Input, ViewChild } from '@angular/core';
import { OrderResponse } from '../../../core/models/order';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { CartService } from '../../../core/services/cart.service';
import { CommonModule } from '@angular/common';
import { Product } from '../../../core/models/product';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language.service';
import { FactureResponse } from '../../../core/models/facture';
import { environment } from '../../../../environments/environment';
import { G } from '@angular/cdk/overlay.d-BdoMy0hX';
import { ConfigGlobalService } from '../../../core/services/config-global.service';

@Component({
  selector: 'app-invoice-template',
  imports: [CurrencyFormatPipe,CommonModule,TranslateModule],
  templateUrl: './invoice-template.component.html',
  styleUrl: './invoice-template.component.css'
})
export class InvoiceTemplateComponent {
  cartService$ = inject(CartService);
  languageService = inject(LanguageService)
  gcService = inject(ConfigGlobalService)
  imagesBaseUrl=environment.imageUrlBase
  @Input() facture?: FactureResponse;
  @ViewChild('invoiceTemplateRoot', { static: false }) invoiceElement!: ElementRef;
  shipping = this.gcService.configGlobal().valeurLivraison
  constructor( ){}
 
  getStatusText(status: string): string {
    switch(status) {
      case 'LIVREE': return 'Delivered';
      case 'EN_COURS': return 'In Progress';
      default: return 'Shipped';
    }
  }
  getProductImage(product:Product): string {
    return product.imageProduits[0]?.url || 'https://via.placeholder.com/100';
  }
  calculateTotal(order: OrderResponse | undefined): number {
    if (!order?.produits) return 0;
    return order.produits.reduce((total, product) => total + (product.prixTotal || 0), 0);
  }
  calculateShipping(order: OrderResponse | undefined): number {
    if(this.calculateTotal(order) >= this.gcService.configGlobal().seuilLivraisonGratuite){
      return 0
      
    }
    return this.shipping
  }
}
