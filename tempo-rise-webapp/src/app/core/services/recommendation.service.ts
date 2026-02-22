import { HttpClient } from '@angular/common/http';
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { catchError, Observable, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private baseUrl = `${environment.apiUrl}/model`;
  private recommendationUrl = `${environment.apiUrl}/recommendation`;

  constructor(private http: HttpClient) { }
  
  private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
}
  train(loss: string = 'logistic', learning_rate: number = 0.005, epochs: number = 60, no_components: number = 32): Observable<any> {
    const params = new HttpParams()
    .set('loss', loss)
    .set('learning_rate', learning_rate)
    .set('epochs', epochs)
    .set('no_components', no_components);
      return this.http.get(`${this.baseUrl}/train`,{
        ...this.httpOptions,
        params
      });
    }
    tune(n_trials: number = 30, no_components: number = 32): Observable<any> {
    const params = new HttpParams()
    .set('n_trials', n_trials)
    .set('no_components', no_components)
      return this.http.get(`${this.baseUrl}/tune`,{
        ...this.httpOptions,
        params
      });
    }
  recommend(anonymousUserSessionToken: string | null, userId: number | null, num_recs: number = 3): Observable<any> {
    let params = new HttpParams().set('num_recs', num_recs);

    if (userId != null) {
      params = params.set('userId', userId);
    } else {
      params = params.set('anonymousUserSessionToken', anonymousUserSessionToken ?? "");
    }
    return this.http.get(`${this.recommendationUrl}`, {
      params
    }).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }

    config(): Observable<any> {
      return this.http.get(`${this.baseUrl}/config`, this.httpOptions);
    }
    metrics(): Observable<any> {
      return this.http.get(`${this.baseUrl}/metrics`, this.httpOptions);
    }


}
