import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
interface ResetPasswordRequest {
  newPassword: string;
  confirmPassword: string;
}
@Injectable({
  providedIn: 'root'
})
export class ResetPasswordService {

  constructor(private http: HttpClient) { }

  resetPassword(resetPasswordRequest:ResetPasswordRequest): Observable<any> {
    return this.http.post(`${environment.apiUrl}/reset-password`,resetPasswordRequest);
  }
  }