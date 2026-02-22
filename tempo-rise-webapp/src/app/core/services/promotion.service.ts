import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Promotion } from '../models/promotion';
import { Response } from '../models/response';
import { environment } from '../../../environments/environment';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class PromotionService {
  private promotionsUrl = `${environment.apiUrl}/promotions`;
  private promotionsByProductIdUrl = `${environment.apiUrl}/promotionsByProductId`;

  constructor(private http: HttpClient) { }

  createPromotion(promotionRequest: any): Observable<Promotion> {
    return this.http.post<Promotion>(this.promotionsUrl, promotionRequest, httpOptions);
  }

  deleteAllPromotions(): Observable<Response> {
    return this.http.delete<Response>(this.promotionsUrl, httpOptions);
  }

  deletePromotion(id: number): Observable<Response> {
    const url = `${this.promotionsUrl}/${id}`;
    return this.http.delete<Response>(url, httpOptions);
  }

  getAllPromotions(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(this.promotionsUrl, httpOptions);
  }

  getPromotionById(id: number): Observable<Promotion> {
    const url = `${this.promotionsUrl}/${id}`;
    return this.http.get<Promotion>(url, httpOptions);
  }
  getActivePromotions(): Observable<Promotion[]> {
    const url = `${this.promotionsUrl}/active`;
    return this.http.get<Promotion[]>(url, httpOptions);
  }
  getInActivePromotions(): Observable<Promotion[]> {
    const url = `${this.promotionsUrl}/inactive`;
    return this.http.get<Promotion[]>(url, httpOptions);
  }

  getPromotionsByProductId(id: number): Observable<Promotion[]> {
    const url = `${this.promotionsByProductIdUrl}/${id}`;
    return this.http.get<Promotion[]>(url, httpOptions);
  }

  updatePromotion(id: number, productData: any): Observable<Promotion> {
    const url = `${this.promotionsUrl}/${id}`;    
    return this.http.put<Promotion>(url, productData, httpOptions);
  }
}