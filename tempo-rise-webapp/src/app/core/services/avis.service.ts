import { Injectable, signal, WritableSignal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { finalize, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AvisService {
  private baseUrl = `${environment.apiUrl}/avis`;
  private avisByProductIdUrl = `${environment.apiUrl}/avisByProductId`;
  private avisByUserIdUrl = `${environment.apiUrl}/avisByUserId`;
  private isLoading : WritableSignal<boolean> = signal(false);
  readonly isLoading$ = this.isLoading.asReadonly();
  constructor(private http: HttpClient) { }

  // Default HTTP options with credentials
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }

  createAvis(avisRequest: any): Observable<any> {
    return this.http.post(this.baseUrl, avisRequest, this.httpOptions);
  }

  deleteAllAvis(): Observable<any> {
    return this.http.delete(this.baseUrl, this.httpOptions);
  }

  deleteAvis(id: number): Observable<any> {
    const url = `${this.baseUrl}/${id}`;
    return this.http.delete(url, this.httpOptions);
  }

  getAllAvis(): Observable<any> {
    this.isLoading.set(true);
    return this.http.get(this.baseUrl, this.httpOptions).pipe(
      finalize(() => {
        // ✅ Runs when request + inner observable both complete or error
        this.isLoading.set(false);
      })
    );
  }

  getAvisById(id: number): Observable<any> {
    const url = `${this.baseUrl}/${id}`;
    return this.http.get(url, this.httpOptions);
  }

  getAvisByProductId(id: number): Observable<any> {
    const url = `${this.avisByProductIdUrl}/${id}`;
    return this.http.get(url);
  }
  getAvisByProductIdPaged(id: number, page: number, size: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/product/${id}?page=${page}&size=${size}`);
  }
  // avis.service.ts
getPagedAvis(params: any) {
    const queryParams:any = {
    page: params.page ?? 0,
    size: params.size ?? 10,
  };

  if(params.userEmail) queryParams.userEmail = params.userEmail;
  if(params.comment) queryParams.comment = params.comment;
  if(params.productName) queryParams.productName = params.productName;

  return this.http.get<any>(`${this.baseUrl}/paged/filtered`, { params: queryParams });
}

  getAvisByUserId(id: number): Observable<any> {
    const url = `${this.avisByUserIdUrl}/${id}`;
    return this.http.get(url, this.httpOptions);
  }

  updateAvis(id: number, avisRequest: any): Observable<any> {
    const url = `${this.baseUrl}/${id}`;
    return this.http.put(url, avisRequest, this.httpOptions);
  }
}