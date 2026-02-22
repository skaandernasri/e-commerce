import { Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, finalize, map, Observable, switchMap, tap, throwError } from 'rxjs';
import { Product } from '../models/product';
import { ProductService } from './product.service';
export interface WhishlistDto{
  id: number;
  utilisateurId: number;
  createdAt: string;
  produits: Product[]
}
@Injectable({
  providedIn: 'root'
})
export class WhishlistService {
  private baseUrl = `${environment.apiUrl}/whishlist`;
  private wishlist$: WritableSignal<WhishlistDto> = signal<WhishlistDto>( {
    id: 0,
    utilisateurId: 0,
    createdAt: '',
    produits: []
  });
  wishlist = this.wishlist$?.asReadonly();
  isLoading$ = signal<boolean>(false);
  constructor(private http: HttpClient,private productService:ProductService) { }
    private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
}

getWhishlistByUserId(userId: number): Observable<WhishlistDto> {
  this.isLoading$.set(true);

  return this.http.get<WhishlistDto>(`${this.baseUrl}/${userId}`, this.httpOptions).pipe(
    tap(finalResponse => {
      this.wishlist$?.set(finalResponse);
    }),
    finalize(() => {
      // ✅ Runs when request + inner observable both complete or error
      this.isLoading$.set(false);
    }),
    catchError(error => {
      console.error('Error fetching whishlist:', error);
      return throwError(() => error);
    })
  );
}



  addItemToWhishlist( userId: number, productId: number): Observable<any> {
    this.isLoading$.set(true);
    return this.http.post<any>(`${environment.apiUrl}/product/${productId}/whishlist/user/${userId}`, this.httpOptions).pipe(
      tap(response => {
        this.wishlist$?.update((whishlist) => ({
          ...whishlist,
          produits: [...whishlist.produits, response.produit]
        }));
        this.isLoading$.set(false);
      }),
      catchError(error => {
        if(error.status!=400){
        console.error('Error adding item to whishlist:', error);
        }
        this.isLoading$.set(false);
        return throwError(() => error);
        
      })
    );
  }
  removeItemFromWhishlist( userId: number, productId: number): Observable<any> {
    this.isLoading$.set(true);
    return this.http.delete<any>(`${environment.apiUrl}/product/${productId}/whishlist/user/${userId}`, this.httpOptions).pipe(
      tap(response => {
        this.wishlist$?.update((whishlist) => ({
          ...whishlist,
          produits: whishlist.produits.filter(product => product.id !== productId)
        }));
        this.isLoading$.set(false);
      }),
      catchError(error => {
        console.error('Error removing item from whishlist:', error);
        this.isLoading$.set(false);
        return throwError(() => error);
      })
    );
  }
  removeAllItemsFromWhishlist( userId: number): Observable<any> {
    this.isLoading$.set(true);
    return this.http.delete<any>(`${environment.apiUrl}/whishlist/${userId}`, this.httpOptions).pipe(
      tap(response => {
        this.wishlist$?.update((whishlist) => ({
          ...whishlist,
          produits: []
        }));
        this.isLoading$.set(false);
      }),
      catchError(error => {
        console.error('Error removing all items from whishlist:', error);
        this.isLoading$.set(false);
        return throwError(() => error);
      })
    );
  }
}
