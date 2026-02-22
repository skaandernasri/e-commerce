import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { InvoiceRequest, InvoiceResponse } from '../models/invoice';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
    private baseUrl = `${environment.apiUrl}/factures`;
  

  constructor(private http: HttpClient) { }
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }
  createInvoice(invoiceRequest: InvoiceRequest): Observable<InvoiceResponse> {
      return this.http.post<InvoiceResponse>(this.baseUrl, invoiceRequest, this.httpOptions);
    }
  
    deleteAllInvoices(): Observable<Response> {
      return this.http.delete<Response>(this.baseUrl, this.httpOptions);
    }
  
    deleteInvoice(id: number): Observable<Response> {
      const url = `${this.baseUrl}/${id}`;
      return this.http.delete<Response>(url, this.httpOptions);
    }
  
    getAllInvoices(): Observable<InvoiceResponse[]> {
      return this.http.get<InvoiceResponse[]>(this.baseUrl, this.httpOptions);
    }
  
    getInvoiceById(id: number): Observable<InvoiceResponse> {
      const url = `${this.baseUrl}/${id}`;
      return this.http.get<InvoiceResponse>(url, this.httpOptions);
    }
  
    getInvoiceByOrderId(id: number): Observable<InvoiceResponse> {
      const url = `${this.baseUrl}/commande/${id}`;
      return this.http.get<InvoiceResponse>(url, this.httpOptions);
    }
  
    getInvoiceByUserId(id: number): Observable<any> {
      const url = `${this.baseUrl}/utilisateur/${id}`;
      return this.http.get(url, this.httpOptions);
    }
  
}
