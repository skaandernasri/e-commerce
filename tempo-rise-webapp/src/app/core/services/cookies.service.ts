import { Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CookiesConsentService {
 private readonly cookieExists : WritableSignal<boolean> = signal(false);
 readonly cookieExists$ = this.cookieExists.asReadonly();
url = environment.apiUrl + '/cookieConsent';
  constructor(private http: HttpClient) {
  }
  getOrCreateCookieConsent(value:string | null): Observable<any> {
    return this.http.post<any>(this.url,{value},{withCredentials: true}).pipe(tap((response) => {
      this.cookieExists.set(response.cookieConsentExist);
    }),catchError((error) => {
            return throwError(() => error);
          }),);
  }

  accept() {
    this.getOrCreateCookieConsent('ACCEPTED').subscribe();
  }

  decline() {
    this.getOrCreateCookieConsent('DECLINED').subscribe();
  }
}
