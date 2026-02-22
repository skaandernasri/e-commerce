import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ProductService } from './product.service';
import { forkJoin, map, Observable, switchMap, tap } from 'rxjs';
import { FactureResponse } from '../models/facture';

@Injectable({
  providedIn: 'root'
})
export class FactureService {
  private baseUrl = `${environment.apiUrl}/factures`;

  constructor(private http:HttpClient
  ) { }
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }
  getFactureByUserId(userId: number): Observable<FactureResponse[]> {
    const url = `${this.baseUrl}/utilisateur/${userId}`;
    return this.http.get<FactureResponse[]>(url, this.httpOptions);
  }
  getFactureByOrderId(orderId: number): Observable<FactureResponse> {
    const url = `${this.baseUrl}/commande/${orderId}`;
    return this.http.get<FactureResponse>(url, this.httpOptions);
  }

}
