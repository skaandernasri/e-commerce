import { computed, inject, Injectable, OnDestroy, signal, WritableSignal } from '@angular/core';
import {  catchError, filter, firstValueFrom, forkJoin, map, Observable, of, Subject, switchMap, take, takeUntil, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Cart, CartProduct } from '../models/cart';
import { Product, variantResponse } from '../models/product';
import { ProductService } from './product.service';
import { SuiviClientService } from './suivi-client.service';
import { SwalService } from './swal-service.service';
import { AuthService } from './auth.service';
import { ConfigGlobalService } from './config-global.service';
import { MetaPixelService } from './meta-pixel.service';

@Injectable({ providedIn: 'root' })
export class CartService implements OnDestroy {
  //#region Signals & State
  private readonly metaPixelService = inject(MetaPixelService);
  private globalConfig = inject(ConfigGlobalService);
  public isOpen = signal<boolean>(false);
  private _cartItemCount = signal<number>(0);
  public readonly cartItemCount = this._cartItemCount.asReadonly();
  private _cart = signal<Cart>({ id: 0, articles: [] });
  public readonly cart = this._cart.asReadonly();
  private _guestCart = signal<Cart>({ id: 0, articles: [] });
  public readonly guestCart = this._guestCart.asReadonly();
  private _guestCartItemCount = signal<number>(0);
  public readonly guestCartItemCount = this._guestCartItemCount.asReadonly();
  private _subtotal = signal<number>(0);
  public readonly subtotal = this._subtotal.asReadonly();
  private _shipping = computed(() => this.subtotal() >= this.globalConfig.configGlobal().seuilLivraisonGratuite ? 0 : this.globalConfig.configGlobal().valeurLivraison);
  public readonly shipping = this._shipping;
  private _total = computed(() => this._subtotal() + this._shipping());
  public readonly total = this._total;
  public readonly isEmpty=computed(() => this._cartItemCount()===0);
  public readonly isGuestCartEmpty=computed(() => this._guestCartItemCount()===0);
  //#endregion
  private authService = inject(AuthService);
  //#region Properties
  private readonly cartLock = new Subject<boolean>();
  isLoading = signal<boolean>(false);
  public addToCartButtonIsLoading = false;
  destroy$ = new Subject<void>();
  //#endregion

  //#region Constructor & Initialization
  constructor(
    private readonly http: HttpClient,
    private readonly productService: ProductService,
    private suiviClientService: SuiviClientService,
    private swalService: SwalService
  ) {}
  //#endregion

  //#region HTTP Configuration
  private get httpOptions() {
    return {
      withCredentials: true,
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
  }
  //#endregion

  //#region Cart Initialization & Management
  createAndGetCart(userId: number): Observable<Cart> {
    return this.http.get<Cart>(`${environment.apiUrl}/panier/utilisateur/${userId}`).pipe(
      catchError(error => {
        if (error.status === 404) {
          return this.http.post<Cart>(`${environment.apiUrl}/panier`, { utilisateurId: userId }).pipe(
            tap(newCart => this.updateCartState(newCart)));
        }
  this.showErrorNotification('SWAL.CART.FAILED_RETRIEVE');
        return throwError(() => error);

      }),
      tap(cart => {
        if (!cart.articles) cart.articles = [];
        
      }),
      tap(cart => this.updateCartState(cart))
    );
  }

  mergeCarts(userId: number): Observable<Cart> {
    const guestcart = this.guestCart().articles.flatMap(product =>
      product.variants.map(variant => ({
        id: variant.id,
        quantite: variant.cartQuantity ?? 0,
      }))
    );
    return this.http.post<Cart>(`${environment.apiUrl}/utilisateur/${userId}/panier/merge`, guestcart).pipe(
      tap(mergedCart => {        
        this.resetProductQuantities();
        this.updateCartState(mergedCart);
        this.removeAllItemsFromGuestCart();
      })
    );
  }
  //#endregion

  //#region Guest Cart Operations
  updateGuestCart(cart: Cart): void {
      this.setCartAndSave(cart);
  }

  addItemToGuestCart(product: Product, variant: variantResponse, quantite: number = 1): void {
    if (quantite <= 0) throw new Error('Quantity must be positive');
    const available = variant.availableQuantity ?? 0;
    const cart = this.guestCart() as Cart;
    const existingProduct = cart.articles.find(p => p.id === product.id) as CartProduct | undefined;
    let totalQuantityInCart = quantite;
    if (existingProduct) {
      const existingVariant = existingProduct.variants.find(v => v.id === variant.id);
      if (existingVariant) {
        const total = existingVariant.cartQuantity + quantite;
        if (total > available) throw new Error('Not enough quantity available');
        existingVariant.cartQuantity = total;
        totalQuantityInCart = total;
      } else {
        if (quantite > available) throw new Error('Not enough quantity available');
        existingProduct.variants.push({ ...variant, cartQuantity: quantite });
      }
    } else {
      const newProduct: CartProduct = {
        ...product,
        variants: [{ ...variant, cartQuantity: quantite }],
        newPrice: this.productService.calculateDiscountedPrice(product),
        prixTotal: this.productService.calculateDiscountedPrice(product) * quantite
      };
      cart.articles.push(newProduct);
    }

    this.suiviClientService.addAction({
      produitId: product.id,
      typeAction: "ADD_TO_CART",
      utilisateurAnonymeUuid: localStorage.getItem('anonyme-session-token')
    });
    this.updateGuestCart(cart);
    this.openCart();
    this.metaPixelService.addAddToCartEvent([product.id.toString()], "product", [{id: product.id.toString(), quantity: totalQuantityInCart}], 'TND', this.productService.calculateDiscountedPrice(product));
  }

  decreaseOrRemoveItemFromGuestCart(product: Product, variant: variantResponse, quantite: number = 1): void {
    const cart = this.guestCart();
    const existingProduct = cart.articles.find(p => p.id === product.id) as CartProduct | undefined;
    if (!existingProduct) return;

    const existingVariant = existingProduct.variants.find(v => v.id === variant.id);
    if (!existingVariant) return;

    existingVariant.cartQuantity -= quantite;
    if (existingVariant.cartQuantity <= 0) {
      existingProduct.variants = existingProduct.variants.filter(v => v.id !== variant.id);
    }

    if (existingProduct.variants.length === 0) {
      cart.articles = cart.articles.filter(p => p.id !== product.id);
    }

    this.updateGuestCart(cart);
  }

  removeItemFromGuestCart(product: Product, variantToRemove: variantResponse): void {
    const cart = this.guestCart();
    const itemToUpdate = cart.articles.find(item => item.id === product.id);
    if (!itemToUpdate) return;

    this.suiviClientService.addAction({
      produitId: product.id,
      typeAction: "REMOVE_FROM_CART",
      utilisateurAnonymeUuid: localStorage.getItem('anonyme-session-token')
    });

    const variantIndex = itemToUpdate.variants.findIndex(v => v.id === variantToRemove.id);
    if (variantIndex === -1) return;

    const removedVariant = itemToUpdate.variants[variantIndex];
    this.productService.updateProductQuantities([{
      id: product.id,
      variantId: removedVariant.id as number,
      change: removedVariant.cartQuantity ?? 0
    }]);

    itemToUpdate.variants.splice(variantIndex, 1);
    if (itemToUpdate.variants.length === 0) {
      cart.articles = cart.articles.filter(item => item.id !== product.id);
    }

    this.updateGuestCart(cart);
  }

  clearGuestCart(): void {
    localStorage.removeItem('guest_cart');
    this._guestCart.set({ id: 0, articles: [] });
    this._guestCartItemCount.set(0);
    this.calculateTotalsGuest([]);
  }
  clearCart(): void {
    this._cart.set({ id: 0, articles: [] });
    this._cartItemCount.set(0);
    this.calculateTotals([]);
  }

  removeAllItemsFromGuestCart(): void {
    const guestCart = this.guestCart();
    guestCart.articles.forEach(item => {
      this.suiviClientService.addAction({
        produitId: item.id,
        typeAction: "REMOVE_FROM_CART",
        utilisateurAnonymeUuid: localStorage.getItem('anonyme-session-token')
      });
    })
    this.updateGuestCart({ id: 0, articles: [] });   
    this._guestCart.set({ id: 0, articles: [] });
    this._guestCartItemCount.set(0);
    this.resetProductQuantities();
  }
  //#endregion

  //#region User Cart Operations
  addItemToCart(product: Product, variant: variantResponse, quantite: number = 1, userId?: number): void {
    
    if (!userId) {
  this.showErrorNotification('SWAL.CART.USER_ID_REQUIRED');
      return;
    }
    if (quantite <= 0) {
  this.showErrorNotification('SWAL.CART.QUANTITY_POSITIVE');
      return;
    }
    if ((variant.availableQuantity ?? 0) < quantite) {
  this.showErrorNotification('SWAL.CART.NOT_ENOUGH_QUANTITY');
      return;
    }

    const cart = this.cart() as Cart;
    const existingProduct = cart.articles.find(p => p.id === product.id) as CartProduct | undefined;
    
    if (existingProduct) {
      const existingVariant = existingProduct.variants.find(v => v.id === variant.id);
      if (existingVariant) {
        const totalQuantity = existingVariant.reservedQuantity! + quantite;
        if ((variant.quantity ?? 0) < totalQuantity) {
  this.showErrorNotification('SWAL.CART.NOT_ENOUGH_QUANTITY');
          return;
        }
        this.updateCartVariantQuantity(userId, product, variant, totalQuantity, cart);
      } else {
        this.addNewVariantToCartProduct(userId, product, variant, quantite, cart);
      }
    } else {
      this.addNewProductToCart(userId, product, variant, quantite, cart);
    }
  }

  decreaseOrRemoveItemFromCart(product: Product, variant: variantResponse, quantite: number = 1, userId?: number): Observable<any> {
    if (!userId) return throwError(() => new Error('User ID is required'));
    const cart = this.cart() as Cart;
    const cartProduct = cart.articles.find(p => p.id === product.id) as CartProduct | undefined;
    if (!cartProduct) return throwError(() => new Error('Product not found in cart'));

    const cartVariant = cartProduct.variants.find(v => v.id === variant.id);
    if (!cartVariant) return throwError(() => new Error('Variant not found in cart'));

    const newQuantity = cartVariant.reservedQuantity! - quantite;
    if (newQuantity > 0) {
      this.isLoading.set(true);
      return this.updateItem(userId, variant.id!, newQuantity).pipe(
        tap(() => {
          this.updateCartState(cart, {
            productId: product.id,
            variantId: variant.id!,
            newQuantity
          });
          this.isLoading.set(false);
        }),
        catchError(error => {
          this.isLoading.set(false);
            this.showErrorNotification('SWAL.CART.FAILED_UPDATE');

          return throwError(() => error);
        })
      );
    } else {
      return this.removeVariantFromCart(product, variant, userId);
    }
  }

removeVariantFromCart(product: Product, variant: variantResponse, userId: number): Observable<any> {
  const cart = this.cart() as Cart;
  const cartProduct = cart.articles.find(p => p.id === product.id) as CartProduct | undefined;
  if (!cartProduct) return throwError(() => new Error('Product not found in cart'));

  return this.removeItem(userId, variant.id!).pipe(
    tap(() => {
      const updatedArticles = cart.articles
        .map(p => {
          if (p.id === product.id) {
            const updatedVariants = p.variants.filter(v => v.id !== variant.id);
            return updatedVariants.length > 0 ? { ...p, variants: updatedVariants } : null;
          }
          return p;
        })
        .filter(Boolean) as CartProduct[];

      const updatedCart = { ...cart, articles: updatedArticles };
      this.updateCartState(updatedCart);
    }),
    catchError(error => {
      this.showErrorNotification('SWAL.CART.FAILED_UPDATE');
      return throwError(() => error);
    })
  );
}
removeAllItemsFromCart(userId?: number): Observable<any> {
  if (!userId) return throwError(() => new Error('User ID is required'));
  const cart = this.cart();
  
  return this.removeAllItems(userId, cart.id).pipe(
    tap(() => {
      const emptyCart = { id: cart.id, articles: [] };
      this._cart.set(emptyCart);
      this._cartItemCount.set(0);
      this.calculateTotals([]);
      // Clear all cart quantities from products
      this.clearAllProductCartQuantities();
    }),
    catchError(error => {
      this.showErrorNotification('SWAL.CART.FAILED_UPDATE');
      return throwError(() => error);
    })
  );
}
  //#endregion

  //#region API Operations
  removeAllItems(userId: number, cartId: number): Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/utilisateur/${userId}/panier/${cartId}`, this.httpOptions);
  }

  removeItem(userId: number, productId: number): Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/utilisateur/${userId}/panier/items/${productId}`, this.httpOptions);
  }

  addItem(userId: number, variantId: number, quantite: number = 1): Observable<any> {
    return this.http.post(
      `${environment.apiUrl}/utilisateur/${userId}/panier/add-item/${variantId}`,
      { quantite },
      this.httpOptions
    );
  }

  updateItem(userId: number, variantId: number, quantite: number): Observable<any> {
    return this.http.put(
      `${environment.apiUrl}/utilisateur/${userId}/panier/items/${variantId}`,
      { quantite },
      this.httpOptions
    );
  }
  //#endregion

  //#region Cart State Management
  private setCartAndSave(cart: Cart) {
    const updatedCart = {
      ...cart,
      articles: cart.articles.map(item => ({
        ...item,
        newPrice: this.productService.calculateDiscountedPrice(item)
      }))
    };

    this._guestCart.set(updatedCart);
    localStorage.setItem('guest_cart', JSON.stringify(updatedCart));
    this._guestCartItemCount.set(this.calculateItemsCount(updatedCart));
    this.calculateTotalsGuest(updatedCart.articles);
    this.addToCartButtonIsLoading = false;
  }

 private updateCartState(
  cart: Cart,
  updatedVariantInfo?: { productId: number; variantId: number; newQuantity: number }
): void {    
  let updatedCart = cart;
  
  if (updatedVariantInfo) {
    const { productId, variantId, newQuantity } = updatedVariantInfo;
    const productIndex = cart.articles.findIndex(p => p.id === productId);
    if (productIndex !== -1) {
      const cartProduct = { ...cart.articles[productIndex] };
      const updatedVariants = cartProduct.variants.map(v =>
        v.id === variantId ? { ...v, reservedQuantity: newQuantity } : v
      );
      cartProduct.variants = updatedVariants;
      const updatedArticles = cart.articles.map((p, i) => (i === productIndex ? cartProduct : p));
      updatedCart = { ...cart, articles: updatedArticles };
    }
    this.updateSingleProductQuantity(productId);
  }

  const finalCart = {
    ...updatedCart,
    articles: updatedCart.articles.map(item => ({
      ...item,
      newPrice: this.productService.calculateDiscountedPrice(item),
      prixTotal: this.calculateTotalPriceForProduct(item),
    })),
  };
  
  this._cart.set(finalCart);
  this._cartItemCount.set(this.calculateItemsCount(finalCart));
  this.calculateTotals(finalCart.articles);
  
  // Update product quantities after cart state is set
  
  this.updateProductQuantitiesFromCart(finalCart);
}
private updateSingleProductQuantity(productId: number ): void {
  this.productService.updateProductsState([{ id: productId}]);
}
private updateProductQuantitiesFromCart(cart: Cart): void {
  const currentProducts = this.productService.products();
  
  const updatedProducts = currentProducts.map(product => {
    const cartProduct = cart.articles.find(p => p.id === product.id);
    
    if (cartProduct) {
      const updatedVariants = product.variants.map(variant => {
        const cartVariant = cartProduct.variants.find(v => v.id === variant.id);
        if (cartVariant) {
          return {
            ...variant,
            cartQuantity: this.authService.isLoggedIn() ? (cartVariant.reservedQuantity ?? 0) : (cartVariant.cartQuantity ?? 0)
          };
        }
        return { ...variant, cartQuantity: 0 };
      });
      
      return { ...product, variants: updatedVariants };
    }
    
    // Product not in cart, reset cart quantities
    return {
      ...product,
      variants: product.variants.map(v => ({ ...v, cartQuantity: 0 }))
    };
  });
    this.productService.updateProductsState(updatedProducts);

}

private resetProductQuantities() {
  this.isLoading.set(true);
  const currentProducts = this.productService.products();
  const cart = this.authService.isLoggedIn() ? this.cart() : this.guestCart();
  
  this.productService.getAllProducts().pipe(
    takeUntil(this.destroy$)
  ).subscribe(originalProducts => {
    const updatedProducts = currentProducts.map(currentProduct => {
      const originalProduct = originalProducts.find(p => p.id === currentProduct.id);
      if (!originalProduct) return currentProduct;
      
      // Find if this product is in the cart
      const cartProduct = cart.articles.find(p => p.id === currentProduct.id);
      
      if (cartProduct) {
        // Update variants with cart quantities
        const updatedVariants = originalProduct.variants.map(origVariant => {
          const cartVariant = cartProduct.variants.find(v => v.id === origVariant.id);
          if (cartVariant) {
            return {
              ...origVariant,
              cartQuantity: this.authService.isLoggedIn() ? (cartVariant.reservedQuantity ?? 0) : (cartVariant.cartQuantity ?? 0)
            };
          }
          return origVariant;
        });
        
        return { ...originalProduct, variants: updatedVariants };
      }
      
      return originalProduct;
    });
    
    this.productService.updateProductsState(updatedProducts);
    this.isLoading.set(false);
  });
}
private clearAllProductCartQuantities(): void {
  const currentProducts = this.productService.products();
  const updatedProducts = currentProducts.map(product => ({
    ...product,
    variants: product.variants.map(v => ({ ...v, cartQuantity: 0 }))
  }));
  this.productService.updateProductsState(updatedProducts);
}
  //#endregion

  //#region Helper Methods
  private calculateItemsCount(cart: Cart) {
    return cart.articles.reduce((count, product) => count + (product.variants?.length || 0), 0);
  }

  private calculateTotalPriceForProduct(product: CartProduct): number {
    const price = product.newPrice ?? product.prix ?? 0;
    const qty = product.variants.reduce((acc, v) => acc + (v.reservedQuantity ?? 0), 0);
    return price * qty;
  }

  private updateCartVariantQuantity(
    userId: number,
    product: Product,
    variant: variantResponse,
    newQuantity: number,
    cart: Cart
  ): void {
    this.isLoading.set(true);
    this.updateItem(userId, variant.id!, newQuantity).pipe(
      tap(() => this.updateCartState(cart, {
        productId: product.id,
        variantId: variant.id!,
        newQuantity
      })),
      catchError(err => {
  this.showErrorNotification('SWAL.CART.FAILED_UPDATE');
        return throwError(() => err);
      }),
      tap(() => {this.isLoading.set(false); this.openCart();})
    ).subscribe();
  }

  private addNewVariantToCartProduct(
    userId: number,
    product: Product,
    variant: variantResponse,
    quantite: number,
    cart: Cart
  ): void {
    this.isLoading.set(true);
    const productIndex = cart.articles.findIndex(p => p.id === product.id);
    if (productIndex === -1) {
      this.isLoading.set(false);
      return;
    }

    this.addItem(userId, variant.id!, quantite).pipe(
      tap(() => {
        const updatedProduct = { ...cart.articles[productIndex] };
        updatedProduct.variants = [
          ...updatedProduct.variants,
          { ...variant, reservedQuantity: quantite, cartQuantity: quantite },
        ];
        const updatedArticles = [...cart.articles];
        updatedArticles[productIndex] = updatedProduct;
        const updatedCart = { ...cart, articles: updatedArticles };
        this.updateCartState(updatedCart);
        this.metaPixelService.addAddToCartEvent([product.id.toString()], 'product', [{id: product.id.toString(), quantity: quantite }], 'TND', this.calculateTotalPriceForProduct(updatedProduct));
      }),
      catchError(err => {
  this.showErrorNotification('SWAL.CART.FAILED_UPDATE');
        return throwError(() => err);
      }),
      tap(() => {this.isLoading.set(false); this.openCart();})
    ).subscribe();
  }

  private addNewProductToCart(
    userId: number,
    product: Product,
    variant: variantResponse,
    quantite: number,
    cart: Cart
  ): void {
    this.isLoading.set(true);
    const newCartProduct: CartProduct = {
      ...product,
      variants: [{ ...variant, cartQuantity: quantite, reservedQuantity: quantite }],
      newPrice: this.productService.calculateDiscountedPrice(product),
      prixTotal: this.productService.calculateDiscountedPrice(product) * quantite,
    };

    this.addItem(userId, variant.id!, quantite).pipe(
      tap(() => {
        cart.articles.push(newCartProduct);
        this.updateCartState(cart);
        this.metaPixelService.addAddToCartEvent([product.id.toString()], 'product', [{id: product.id.toString(), quantity: quantite }], 'TND', this.calculateTotalPriceForProduct(newCartProduct));
      }),
      catchError(err => {
  this.showErrorNotification('SWAL.CART.FAILED_UPDATE');
        return throwError(() => err);
      }),
      tap(() => {this.isLoading.set(false); this.openCart();})
    ).subscribe();
  }
  //#endregion

  //#region Price Calculations
  calculateTotalsGuest(items: Product[]): void {
    const newSubtotal = items.reduce((sum, item) => {
      const price = item.newPrice ?? item.prix ?? 0;
      const totalQty = item.variants.reduce((acc, v) => acc + (v.cartQuantity ?? 0), 0);
      return sum + (price * totalQty);
    }, 0);

    this._subtotal.set(newSubtotal);
    //this._total.set(newSubtotal + this.shipping());
  }

  calculateTotals(items: Product[]): void {
    
    const newSubtotal = items.reduce((sum, item) => {
      const price = item.newPrice ?? item.prix ?? 0;
      
      const totalQty = item.variants.reduce((acc, v) => acc + (v.reservedQuantity ?? 0), 0);
      return sum + (price * totalQty);
    }, 0);

    this._subtotal.set(newSubtotal);
    //this._total.set(newSubtotal + this.shipping());
  }
  //#endregion


  //#region Getters & Lifecycle
  get getCart(): Cart {
    return this.cart();
  }

  get getGuestCart(): Cart {
    return this.guestCart();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  //#endregion
  //#region UI Operations
toggleCart(): void {
  this.isOpen.update(isOpen => !isOpen);
  document.body.style.overflow = this.isOpen() ? 'hidden' : 'auto';
}

openCart(): void {
  if(this.isOpen()) return
  this.isOpen.set(true);
  document.body.style.overflow = 'hidden';
}
closeCart(): void {
  if(!this.isOpen()) return
  this.isOpen.set(false);
  document.body.style.overflow = 'auto';
}



// Error notification
showErrorNotification(message: string) {
  this.swalService.error(message);
}

  //#endregion

}