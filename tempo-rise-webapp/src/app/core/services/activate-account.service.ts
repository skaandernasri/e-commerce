import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ActivateAccountService {

  private baseUrl = `${environment.apiUrl}/activate`
  constructor( private http:HttpClient) { }
    activate(token:string): Observable<any> {
      return this.http.get(`${this.baseUrl}`,{params:{token}});
    }
}
