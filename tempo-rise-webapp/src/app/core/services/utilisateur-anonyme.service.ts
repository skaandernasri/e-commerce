import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { UserAnonymeRequest, UserAnonymeResponse } from '../models/user';
import { catchError, map, Observable, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurAnonymeService {

  constructor(private http: HttpClient) { }
  url = environment.apiUrl + '/utilisateur/anonyme';
  private readonly sessionToken$ = signal<string>('');
  readonly sessionToken = this.sessionToken$.asReadonly();
  private readonly id$ = signal<number | null>(null);
  readonly id = this.id$.asReadonly();
getOrCreateUtilisateurAnonyme(
    userAnonymeRequest: UserAnonymeRequest
  ): Observable<string> {
    return this.http.post<UserAnonymeResponse>(this.url,userAnonymeRequest, {withCredentials: true}).pipe(
      tap((response) => {
        this.sessionToken$.set(response.sessionToken);
        this.id$.set(response.id);
      }),
      catchError((error) => {
        return throwError(() => error);
      }),
      map(user => user.sessionToken)
    );
  }
}
