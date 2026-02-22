import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Response } from '../models/response';
import { CodePromo } from '../models/code-promo';

@Injectable({
  providedIn: 'root'
})
export class CodePromoService {
  private baseUrl = `${environment.apiUrl}/codePromo`;
  constructor(private http:HttpClient) { }
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }


  createCodePromo(codePromoRequest: CodePromo): Observable<CodePromo> {
      return this.http.post<CodePromo>(this.baseUrl, codePromoRequest, this.httpOptions);
    }
  
    deleteAllCodePromo(): Observable<Response> {
      return this.http.delete<Response>(`${this.baseUrl}/all`, this.httpOptions);
    }
  
    deleteCodePromo(id: number): Observable<Response> {
      return this.http.delete<Response>(`${this.baseUrl}/${id}`, this.httpOptions);
    }
  
    getAllCodePromo(): Observable<CodePromo[]> {
      return this.http.get<CodePromo[]>(this.baseUrl, this.httpOptions);
    }
  
    getById(id: number): Observable<CodePromo> {
      return this.http.get<CodePromo>(`${this.baseUrl}/${id}`, this.httpOptions);
    }
  
    getActiveCode(): Observable<CodePromo[]> {
      return this.http.get<CodePromo[]>(`${this.baseUrl}/active`, this.httpOptions);
    }
    getInActiveCode(): Observable<CodePromo[]> {
      return this.http.get<CodePromo[]>(`${this.baseUrl}/inactive`, this.httpOptions);
    }
  
    getByCode(code: string): Observable<CodePromo> {
      return this.http.get<CodePromo>(`${this.baseUrl}/code/${code}`, this.httpOptions);
    }
  
    updateCodePromo(id: number, codePromoRequest: CodePromo): Observable<any> {
      const url = `${this.baseUrl}/${id}`;
      return this.http.put(url, codePromoRequest, this.httpOptions);
    }
}
