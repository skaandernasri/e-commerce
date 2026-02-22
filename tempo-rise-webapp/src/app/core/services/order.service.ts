import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError,  Observable, tap, throwError } from 'rxjs';
import { filteredPagedOrderResponse, LigneCommandeRequest, Order, OrderResponse, PaymentDetails, PaymentResponse } from '../models/order';
import { ProductService } from './product.service';
import { Response } from '../models/response';
import { MetaPixelService } from './meta-pixel.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly metaPixelService = inject(MetaPixelService);
  private adminUrl = `${environment.apiUrl}/commande/admin`;
  private baseUrl = `${environment.apiUrl}/commandes`;
  private guestUrl = `${environment.apiUrl}/commande/guest`;
  private orders:WritableSignal<OrderResponse[]> = signal<OrderResponse[]>([])
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);
  totalAmmount = signal<number>(0);
  globalTotalElements = signal<number>(0);
  currentWeekOrders = signal<number>(0);
  lastWeekOrders = signal<number>(0);
  currentWeekOrdersTotalAmount = signal<number>(0);
  lastWeekOrdersTotalAmount = signal<number>(0);
  pagination=signal({
    page:1,
    size:10,
  })
  orders$ = this.orders.asReadonly()
  constructor(private http:HttpClient,
    private productService:ProductService
  ) { }
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }
  createOrder(orderRequest: Order): Observable<PaymentResponse> {
        return this.http.post<PaymentResponse>(this.baseUrl, orderRequest, this.httpOptions).pipe(
          tap(() => (   this.metaPixelService.addPurchaseEvent(orderRequest.email ,"TND",orderRequest.total + orderRequest.shipping,
           orderRequest.produits.flatMap((a) => a.variants.map((v) => ({
  id: a.id.toString(),
  quantity: v.cartQuantity,
}))) )   
            )),
          catchError((error) => {
            console.error('Error adding customer details:', error);
            return throwError(() => error);
          })
        );
      }
  adminCreateOrder(orderRequest: any): Observable<PaymentResponse> {
    const mergedContentsMap = new Map<number, { id: number, quantite: number }>();
    orderRequest.produits.forEach((item:any) => {
  if (mergedContentsMap.has(item.productId)) {
    mergedContentsMap.get(item.productId)!.quantite += item.quantite;
  } else {
    mergedContentsMap.set(item.productId, { id: item.productId, quantite: item.quantite });
  }
});
const mergedContents = Array.from(mergedContentsMap.values());

    console.log("cleanData",mergedContents);
    
    return this.http.post<PaymentResponse>(this.adminUrl, orderRequest, this.httpOptions).pipe(
      tap(() => ( this.metaPixelService.addPurchaseEvent( orderRequest.email, "TND",orderRequest.total + orderRequest.shipping,
        mergedContents.map((a) => (
          {
            id: a.id.toString(),
            quantity: a.quantite,
          }
        ))
            ))),
      catchError((error) => {
        console.error('Error adding customer details:', error);
        return throwError(() => error);
      })
    );
  }
  guestCreateOrder(orderRequest: Order): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(this.guestUrl, orderRequest, this.httpOptions).pipe(
      tap(() => (    this.metaPixelService.addPurchaseEvent( orderRequest.email, "TND",orderRequest.total + orderRequest.shipping,
                orderRequest.produits.flatMap((a) => a.variants.map((v) => ({
                  id: a.id.toString(),
                  quantity: v.cartQuantity,
                }))))
            )),
      catchError((error) => {
        console.error('Error adding customer details:', error);
        return throwError(() => error);
      })
    );
  }
  mergeOrders(userId: number,anonymeUserId: number): Observable<Response> {
    return this.http.post<Response>(`${environment.apiUrl}/mergeCommande`,{idUser:userId,idUserAnonyme:anonymeUserId}, this.httpOptions).pipe(
      catchError((error) => {
        console.error('Error adding customer details:', error);
        return throwError(() => error);
      })
    );
    
  }
  deleteOrder(id: number): Observable<Response> {
    return this.http.delete<Response>(`${this.baseUrl}/${id}`, this.httpOptions).pipe(
      tap(() => {
        this.orders.update(order=> order.filter(order => order.id !== id))
      }),catchError((error) => {
        console.error('Error adding customer details:', error);
        return throwError(() => error);
      })
    );
  }
  updateOrderStatus(id: number, orderRequest: any): Observable<OrderResponse> {
    const url = `${environment.apiUrl}/commandeStatus/${id}`;
    return this.http.put<OrderResponse>(url, orderRequest, this.httpOptions).pipe(
      catchError((error) => {
        console.error('Error adding customer details:', error);
        return throwError(() => error);
      })
    )
  
  }
  getAllOrders(): void {
     this.http.get<OrderResponse[]>(`${this.baseUrl}`, this.httpOptions).pipe(
     ).subscribe({
      next: orders => {
        this.orders.set(orders)
      },
      error: error => console.log(error)
    })
  }
  getPagedFilterdOrders(params: any): void {
    const paramsVerif:any = {};
    Object.keys(params).forEach(key => {
      if (params[key] !== null && params[key] !== undefined) {
        paramsVerif[key] = params[key];
      }
    });
    
     this.http.get<filteredPagedOrderResponse>(`${this.baseUrl}/paged/filtered`, {params: paramsVerif, withCredentials: true}).pipe(
      
    ).subscribe({
      next: response => {
        this.orders.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.totalAmmount.set(response.totalAmmount);
        this.globalTotalElements.set(response.globalTotalElements);
        this.currentWeekOrders.set(response.currentWeekOrders);
        this.lastWeekOrders.set(response.lastWeekOrders);
        this.currentWeekOrdersTotalAmount.set(response.currentWeekOrdersTotalAmount);
        this.lastWeekOrdersTotalAmount.set(response.lastWeekOrdersTotalAmount);
      },
      error: error => console.log(error)
    })
  }
    resetCurrentPage(): void {
  this.pagination.update((pagination) => ({...pagination, page: 1}));
  
}
updateCurrentPage(page: number): void {
  this.pagination.update((pagination) => ({...pagination, page}));
}
  getOrderById(id: number, userId?: number | null): Observable<OrderResponse> {
    if(userId === null || userId === undefined) return this.http.get<OrderResponse>(`${this.baseUrl}/${id}`,{...this.httpOptions})
    return this.http.get<OrderResponse>(`${this.baseUrl}/${id}`,{params:{user_id:userId},...this.httpOptions})
}
getOrderByUserId(userId?: number | null): Observable<OrderResponse[]> {
  if(userId === null || userId === undefined) return this.http.get<OrderResponse[]>(`${this.baseUrl}/utilisateur`,{...this.httpOptions})
  return this.http.get<OrderResponse[]>(`${this.baseUrl}/utilisateur`, {params:{id:userId},...this.httpOptions})
}

getOrderByPaymentRef(paymentRef: string, userId ?: number | null): Observable<OrderResponse> {
  if(userId === null || userId === undefined) return this.http.get<OrderResponse>(`${this.baseUrl}/paymentRef`,{params:{payment_ref:paymentRef},...this.httpOptions})
  return this.http.get<OrderResponse>(`${this.baseUrl}/paymentRef`, {params:{payment_ref:paymentRef,user_id:userId},...this.httpOptions})
}

deleteAllOrders(): Observable<Response> {
  return this.http.delete<Response>(this.baseUrl, this.httpOptions).pipe(
    tap(() => this.orders.set([]))
  );
}
getPaymentRef(payRef: string, userId ?: number | null): Observable<PaymentDetails> {
  if(userId === null || userId === undefined) return this.http.get<PaymentDetails>(`${environment.apiUrl}/paiement`,{params:{payment_ref:payRef},...this.httpOptions})
  return this.http.get<PaymentDetails>(
    `${environment.apiUrl}/paiement`,
    {
      ...this.httpOptions,
      params: { payment_ref: payRef }
    }
  ).pipe(
    catchError((error) => {
      console.error('Error fetching payment details:', error);
      return throwError(() => error);
    })
  );
}
updateCustomerDetails(order_id: number,customerDetails: any): Observable<any> {
  return this.http.put<any>(
    `${environment.apiUrl}/commandeCustomerDetails/${order_id}`,
    customerDetails,
    this.httpOptions
  ).pipe(
    catchError((error) => {
      console.error('Error adding customer details:', error);
      return throwError(() => error);
    })
  );

}
updateOrderItem(order_id: number,orderItem: LigneCommandeRequest): Observable<any> {
  return this.http.put<any>(
    `${environment.apiUrl}/ligneCommande/${order_id}`,
    orderItem,
    this.httpOptions
  ).pipe(
    catchError((error) => {
      console.error('Error adding customer details:', error);
      return throwError(() => error);
    })
  );
}
deleteOrderItem(order_id: number,variant_id: number): Observable<Response> {
  return this.http.delete<Response>(
    `${environment.apiUrl}/ligneCommande/${order_id}/${variant_id}`,
    this.httpOptions
  ).pipe(
    catchError((error) => {
      console.error('Error adding customer details:', error);
      return throwError(() => error);
    })
  );
}
}
