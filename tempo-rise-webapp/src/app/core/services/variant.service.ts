import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { variantResponse } from '../models/product';

@Injectable({
  providedIn: 'root'
})
export class VariantService {
  private baseUrl = `${environment.apiUrl}/variant`;
  constructor(private http: HttpClient) { }
    private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
}
  createOrUpdateVariant(variant: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}`, variant, this.httpOptions).pipe(
      catchError(error => {
        console.error('Error updating variant:', error);
        return throwError(() => error);
      })
    );
  }
  deleteVariant(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`, this.httpOptions).pipe(
      catchError(error => {
        console.error('Error deleting variant:', error);
        return throwError(() => error);
      })
    );
  }
  getVariantById(id:number): Observable<variantResponse> {
    return this.http.get<variantResponse>(`${this.baseUrl}/${id}`, this.httpOptions).pipe(
      catchError(error => {
        console.error('Error fetching variant:', error);
        return throwError(() => error);
      })
    );
  }
}
