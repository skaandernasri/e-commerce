import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Action } from '../models/action';
import { catchError, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SuiviClientService {
  private apiUrl = environment.apiUrl +'/suiviClient';

  constructor(private http: HttpClient) {}
  addAction(actionRequest: Action): void {
    // Transform the data to match backend expectations
     this.http.post(`${this.apiUrl}`, actionRequest).pipe(
      catchError((error) => {        
        console.error('Error adding action:', error);
        throw error;
      })
    ).subscribe();
  }
  mergeActions(actionRequest: Action): void {
    // Transform the data to match backend expectations
    this.http.post(`${this.apiUrl}/merge`,actionRequest).pipe(
      catchError((error) => {
        console.error('Error adding action:', error);
        throw error;
      })
    ).subscribe();
  }
}
