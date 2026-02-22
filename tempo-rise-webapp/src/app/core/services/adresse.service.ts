import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AdresseRequest, AdresseResponse } from '../models/adresse';
import { Observable } from 'rxjs';
import { Response } from '../models/response';

@Injectable({
  providedIn: 'root'
})
export class AdresseService {
  private baseUrl = `${environment.apiUrl}/adresses`;

  constructor(private http: HttpClient) { }
  
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }
  createAdresse(adresseRequest: AdresseRequest): Observable<AdresseResponse> {
      return this.http.post<AdresseResponse>(this.baseUrl, adresseRequest, this.httpOptions);
    }
  
    deleteAllAdresses(): Observable<Response> {
      return this.http.delete<Response>(this.baseUrl, this.httpOptions);
    }
  
    deleteAdresses(id: number): Observable<Response> {
      const url = `${this.baseUrl}/${id}`;
      return this.http.delete<Response>(url, this.httpOptions);
    }
  
    getAllAdresses(): Observable<AdresseResponse[]> {
      return this.http.get<AdresseResponse[]>(this.baseUrl, this.httpOptions);
    }
  
    getAdresseById(id: number): Observable<AdresseResponse> {
      const url = `${this.baseUrl}/${id}`;
      return this.http.get<AdresseResponse>(url, this.httpOptions);
    }
  
    getAdresseByUserId(userId?: number | null): Observable<AdresseResponse[]> {
      const url = `${this.baseUrl}/utilisateur`;
      if(userId === null || userId === undefined) return this.http.get<AdresseResponse[]>(url, this.httpOptions)
      return this.http.get<AdresseResponse[]>(url, {...this.httpOptions, params:{user_id:userId}});
    }
  
    getAdresseByType(type: string): Observable<AdresseResponse[]> {
      const url = `${this.baseUrl}/type/${type}`;
      return this.http.get<AdresseResponse[]>(url, this.httpOptions);
    }
  
    updateAdresse(id: number, adresseRequest: AdresseRequest): Observable<AdresseResponse> {
      const url = `${this.baseUrl}/${id}`;
      return this.http.put<AdresseResponse>(url, adresseRequest, this.httpOptions);
    }
}
