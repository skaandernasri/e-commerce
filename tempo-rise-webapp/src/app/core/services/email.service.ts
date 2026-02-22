import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EmailService {

  constructor(private http: HttpClient) { }

   sendVerifEmail(to: {to: string}): Observable<Response> {
      return this.http.post<Response>(`${environment.apiUrl}/sendVerificationEmail`, to);
    }
    sendResetPasswordEmail(to: {to: string}): Observable<any> {
      return this.http.post(`${environment.apiUrl}/sendResetPasswordEmail`, to);
    }
    sendCodePromoEmail(emailForm:any): Observable<any> {
      return this.http.post(`${environment.apiUrl}/sendPromoCodeEmail`, emailForm);
    }




}
