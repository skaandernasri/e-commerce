import { Injectable, WritableSignal, effect, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  Observable,
  of,
  forkJoin,
  throwError,
  catchError,
  map,
  tap,
  finalize,
} from 'rxjs';
import { Product, ProductPaginatedResponse } from '../models/product';
import { environment } from '../../../environments/environment';
import { Promotion } from '../models/promotion';
import { ImageProductService } from './image-product.service';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private productsSignal: WritableSignal<Product[]> = signal<Product[]>([]);
  products = this.productsSignal.asReadonly();
  private productDetailsSignal: WritableSignal<Product> = signal<Product>({} as Product);
  productDetails = this.productDetailsSignal.asReadonly();
  private recommendationSignal: WritableSignal<Product[]> = signal<Product[]>([]);
  recommendations = this.recommendationSignal.asReadonly();
  private homeProductsSignal: WritableSignal<Product[]> = signal<Product[]>([]);
  homeProducts = this.homeProductsSignal.asReadonly();
  private adminProductsSignal: WritableSignal<Product[]> = signal<Product[]>([]);
  adminProducts = this.adminProductsSignal.asReadonly();
  private headerProductsSignal: WritableSignal<Product[]> = signal<Product[]>([]);
  headerProducts = this.headerProductsSignal.asReadonly();
  isLoading = signal<boolean>(false);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);
  maxPrice = signal<number>(0);
  pagination=signal({
    page:1,
    size:10,
  })
  private apiUrl = `${environment.apiUrl}/produits`;
  private apiUrlPaged = `${environment.apiUrl}/produits/paged/filtered`;
  private prodStockDetailsUrl=`${environment.apiUrl}/prods/stock/details`;
  constructor(
    private http: HttpClient
  ) {}

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl).pipe(
      map(response => 
       response.map(p => ({
          ...p,
          newPrice: this.calculateDiscountedPrice(p)
        }))
      ),
      catchError(error => this.handleError('Error fetching products:', error))
    );
  }

 loadProductsListPage(param: any): void {
  this.isLoading.set(true);
  const params = new HttpParams({ fromObject: param });
    this.http.get<ProductPaginatedResponse>(this.apiUrlPaged, { params }).pipe(
      tap(response => {
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.maxPrice.set(response.maxPrice);
        const products = response.content.map(p => ({
          ...p,
          newPrice: this.calculateDiscountedPrice(p)
        }));
        this.productsSignal.set(products);
        
      
      }),
      finalize(() => this.isLoading.set(false))
    ).subscribe();
    
}
 loadHomeProducts(params: any): void {
    this.isLoading.set(true);
    const httpParams = new HttpParams({ fromObject: params });

    this.http.get<ProductPaginatedResponse>(this.apiUrlPaged, { params: httpParams }).pipe(
      tap(response => {
        const products = response.content.map(p => ({
          ...p,
          newPrice: this.calculateDiscountedPrice(p)
        }));
        this.homeProductsSignal.set(products);
        
      }),
      finalize(() => this.isLoading.set(false))
    ).subscribe();
  }

 loadAdminProducts(param: any): void {
  this.isLoading.set(true);
  const params = new HttpParams({ fromObject: param });
    this.http.get<ProductPaginatedResponse>(this.apiUrlPaged, { params }).pipe(
      tap(response => {
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        const products = response.content.map(p => ({
          ...p,
          newPrice: this.calculateDiscountedPrice(p)
        }));
        this.adminProductsSignal.set(products);
      
      }),
      finalize(() => this.isLoading.set(false))
    ).subscribe();
    
}
loadHeaderProducts(param: any): void {
  const params = new HttpParams({ fromObject: param });
    this.getFilteredProducts(params).pipe(
      tap(response => {
        const products = response.map(p => ({
          ...p,
          newPrice: this.calculateDiscountedPrice(p)
        }));
        this.headerProductsSignal.set(products);
      
      }),
    ).subscribe();
    
}
  // ---- CRUD Methods ----

  getProductById(id: number): Observable<Product> {
    if(this.productsSignal().find(p => p.id === id)){
      return of(this.productsSignal().find(p => p.id === id)!);
    }
    return this.http.get<Product>(`${this.apiUrl}/${id}`).pipe(
      tap(response => {
        const product = {
          ...response,
          newPrice: this.calculateDiscountedPrice(response)
        };
        this.productDetailsSignal.set(product);
        return product
      }),
      catchError(error => this.handleError('Error fetching product:', error))
    );
  }
  // getProductId(id: number): Observable<Product> {
  //   return this.http.get<Product>(`${this.apiUrl}/${id}`).pipe(
  //     tap(response => {
  //       const product = {
  //         ...response,
  //         newPrice: this.calculateDiscountedPrice(response)
  //       };
  //       return product
  //     }),
  //     catchError(error => this.handleError('Error fetching product:', error))
  //   );
  // }
  getProductsStockDetails(stockMoreThan: number, stockLessThan: number,stockEquals: number):Observable<any> {
    return this.http.get<any>(`${this.prodStockDetailsUrl}`,{params:{stockMoreThan,stockLessThan,stockEquals}});
  }
  getFilteredProducts(params: any): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/filtered`, { params }).pipe(

      catchError(error => this.handleError('Error fetching filtered products:', error))
    );
  }
createProduct(productData: any): Observable<Product> {
  return this.http.post<Product>(this.apiUrl, this.createPayload(productData)).pipe(
    tap(newProduct => 
      this.addNewProduct(newProduct, productData)),
          catchError(error => this.handleError('Error creating product:', error))

  );
}

updateProduct(id: number, productData: any): Observable<Product> {
  return this.http.put<Product>(`${this.apiUrl}/${id}`, this.createPayload(productData)).pipe(
    catchError(error => this.handleError('Error updating product:', error))
  );
}



  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => this.handleError('Error deleting product:', error))
    );
  }

  deleteAllProducts(): Observable<void> {
    return this.http.delete<void>(this.apiUrl).pipe(
      tap(() => this.productsSignal.set([])),
      catchError(error => this.handleError('Error deleting all products:', error))
    );
  }

  getProductsByIds(ids: number[]): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/byIds/${ids}`).pipe(
      catchError(error => this.handleError('Error fetching products by IDs: here ids '+ids, error))
    );
  }

updateProductQuantities(updates: { id: number, variantId: number, change: number }[]): void {
  const updated = this.productsSignal().map(product => {
    const productUpdates = updates.filter(u => u.id === product.id);
    if (productUpdates.length === 0) return product;

    const updatedVariants = product.variants.map(variant => {
      const update = productUpdates.find(u => u.variantId === variant.id);
      if (!update) return variant;

      const currentQty = variant.availableQuantity ?? 0;
      return {
        ...variant,
        availableQuantity: currentQty + update.change
      };
    });

    return {
      ...product,
      variants: updatedVariants
    };
  });

  this.productsSignal.set(updated);
}


  updateProductsState(products: any[]): void {

      const productIds = products.map(p => p.id);
      if(!productIds.length) return;
  this.getProductsByIds(productIds).pipe(
    tap(response => {
      const updatedProducts = response.map(p => ({
        ...p,
        newPrice: this.calculateDiscountedPrice(p)
      }));
      this.productsSignal.set(updatedProducts);
      this.productDetailsSignal.update(current => updatedProducts.find(p => p.id === current.id) || current);
      
    })
  ).subscribe();

}


  // ---- Promotion Utilities ----

  hasActivePromotion(product: Product): boolean {
    return product?.promotions?.some(promo => this.isPromotionActive(promo)) || false;
  }

  getCurrentPromotion(product: Product): Promotion | undefined {
    return product?.promotions?.find(promo => this.isPromotionActive(promo));
  }

  calculateDiscountedPrice(product: Product): number {
    if (!product || !this.hasActivePromotion(product)) {
     return product?.prix || 0;}   

    const promotion = this.getCurrentPromotion(product);
    if(promotion?.type== "FIXED"){
      return promotion ? product.prix - promotion.reduction : product.prix;
    }
    return promotion ? product.prix * (1 - promotion.reduction / 100) : product.prix;
  }

  // ---- Helpers ----

  calculateDiscount(products:Product[]): void {
    this.productsSignal.update(current => {
      return current.map(product => ({
        ...product,
        newPrice: this.calculateDiscountedPrice(product)
      }));
    });
  }
    
    


  private createPayload(productData: any): any {
    return {
      nom: productData.nom,
      description: productData.description,
      prix: productData.prix,
      stock: productData.stock,
      categorie: productData.categorie?.id,
      promotion: productData.promotion?.id,
      quantite: productData.quantite,
      composition: productData.composition,
      guide: productData.guide,
      faq: productData.faq,
      marque: productData.marque,
      actif: productData.actif,
      taille: productData.taille,
      couleur: productData.couleur,
      variants: productData.variants
    };
  }

  private isPromotionActive(promo: Promotion): boolean {
    try {
      const now = new Date().toISOString();
      const start = new Date(promo.dateDebut).toISOString();
      const end = new Date(promo.dateFin).toISOString();
      return now >= start && now <= end;
    } catch (e) {
      console.error('Invalid promotion dates', promo, e);
      return false;
    }
  }

  private handleError(message: string, error: any): Observable<never> {
    console.error(message, error);
    return throwError(() => error);
  }


  private addNewProduct(newProduct: Product, productData: any): void {
    newProduct.categorie.nom = productData.categorie?.nom;
    const current = this.productsSignal();
    this.productsSignal.update(current => [...current, newProduct]);
  }

  setProductDetails(product: Product): void {
    this.productDetailsSignal.set(product);
  }
  removeProductDetails(): void {
    this.productDetailsSignal.set({} as Product);
  }

  clearProducts(): void {
    this.productsSignal.set([]);
  }
  resetCurrentPage(): void {
  this.pagination.update((pagination) => ({...pagination, page: 1}));
  
}
  updateCurrentPage(page: number): void {
  this.pagination.update((pagination) => ({...pagination, page}));}
  updateRecommendedProducts(recommendedProducts: Product[]): void {
    this.recommendationSignal.set(recommendedProducts);
  }
  clearHeaderProducts(): void {
    this.headerProductsSignal.set([]);  
  }
}
