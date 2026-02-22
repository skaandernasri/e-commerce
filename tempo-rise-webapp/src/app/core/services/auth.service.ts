import { Injectable, NgZone, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, Subject, catchError, map, of, takeUntil, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SuiviClientService } from './suivi-client.service';
import { MetaPixelService } from './meta-pixel.service';

// declare global {
//   const google: any;
// }

interface AuthResponse {
  code: string;
  message: string;
  details?: any[];
}

interface UserData {
  email: string;
  password: string;
  nom: string;
  roles: string[];
}

export interface AuthState {
  isAuthenticated: boolean;
  roles: string[];
  email: string | null;
  username: string | null;
  id: number | null; 
  expiresAt: number |null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private readonly metaPixelService = inject(MetaPixelService);
  private readonly AUTH_KEY = 'auth_state';
  private readonly AUTH_EXPIRY_MS = 86400000; // 24 hours
  private destroy$ = new Subject<void>();
  private _authState = signal<AuthState>(this.getInitialAuthState());
  authState = this._authState.asReadonly();
  constructor(
    private suiviClientService:SuiviClientService,

  ) {
    this.checkAuthStatus();
    this.initializeTokenRefresh();

  }

  //#region Authentication State Management
  private getInitialAuthState(): AuthState {
    const storedState = localStorage.getItem(this.AUTH_KEY);
    if (!storedState) return this.createEmptyAuthState();
    
    try {
      const parsed = JSON.parse(storedState);
      const isValid = parsed.timestamp && (Date.now() - parsed.timestamp < this.AUTH_EXPIRY_MS);
      return isValid ? parsed : this.createEmptyAuthState();
    } catch {
      return this.createEmptyAuthState();
    }
  }

  private createEmptyAuthState(): AuthState {
    return {
      isAuthenticated: false,
      roles: [],
      email: null,
      username: null,
      id: null,
      expiresAt: 0
    };
  }

  private persistAuthState(state: Partial<AuthState>): void {
    const currentState = this.authState();
    const newState = { ...currentState, ...state, timestamp: Date.now() };
    localStorage.setItem(this.AUTH_KEY, JSON.stringify(newState));
    this._authState.set(newState);
    //this.webSocketService.connect();
      
  }

  public checkAuthStatus(): void {
    this._authState.set(this.getInitialAuthState());
  }

  clearAuthState(): void {
    localStorage.removeItem(this.AUTH_KEY);
    this._authState.set(this.createEmptyAuthState());
    //this.webSocketService.disconnect();
  }
  //#endregion

  //#region Authentication Methods
  signUp(userData: UserData): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/signup`, userData).pipe(takeUntil(this.destroy$),
      tap(() => {
          this.router.navigate(['/auth/user/signin']);
          this.metaPixelService.addLeadEvent("Account creation", userData.email);
      }),
      catchError(error => {
      console.error('Error signing up:', error);
      return throwError(() => error);
    })
    );
  }

// auth.service.ts
signIn(credentials: { email: string; password: string }): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(
    `${environment.apiUrl}/auth/signin`, 
    credentials,
    { withCredentials: true }
  ).pipe(takeUntil(this.destroy$),
    map(response => {
      if (response.code === '200') {
       // this.refreshToken().subscribe();
        const expiresAt = new Date(response.details?.[4]).getTime();
        
        this.persistAuthState({
          isAuthenticated: true,
          roles: response.details?.[1] || [],
          email: credentials.email,
          username: response.details?.[2] || '',
          id: response.details?.[3] || null,
          expiresAt: expiresAt
        });
        
        this.scheduleTokenRefresh(expiresAt);
       this.suiviClientService.mergeActions({utilisateurId: response.details?.[3],
        utilisateurAnonymeUuid:localStorage.getItem('anonyme-session-token')});
      }
      return response;
    }),
    catchError(this.handleError)
  );
}

  logout(): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/auth/logout`,
      { 
        withCredentials: true,
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      }
    ).pipe(
      map(() => {
        this.handleLogoutSuccess();
        return undefined;
      }),
      catchError(error => {
        this.handleLogoutSuccess();
        return throwError(() => error);
      })
    );
  }

  private handleLogoutSuccess(): void {
    const currentUrl = this.router.url;
    this.clearAuthState();
    this.redirectAfterLogout(currentUrl);
  }

  private redirectAfterLogout(currentUrl: string): void {
    const loginPath = currentUrl.startsWith('/admin') 
      ? '/auth/admin/signin' 
      : '/auth/user/signin';
    
    this.router.navigate([loginPath], { 
      queryParams: { returnUrl: currentUrl } 
    });
  }
  //#endregion

  
   

   // Add this method to your AuthService class

handleGoogleSignIn(idToken: string): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(
    `${environment.apiUrl}/auth/signin/google`,
    { idToken },
    { withCredentials: true }
  ).pipe(
    takeUntil(this.destroy$),
    map(response => {
      if (response.code === '200') {
        const expiresAt = new Date(response.details?.[4]).getTime();
        
        this.persistAuthState({
          isAuthenticated: true,
          roles: response.details?.[1] || [],
          email: response.details?.[0] || '',
          username: response.details?.[2] || '',
          id: response.details?.[3] || null,
          expiresAt: expiresAt
        });
        
        this.scheduleTokenRefresh(expiresAt);
        this.suiviClientService.mergeActions({
          utilisateurId: response.details?.[3],
          utilisateurAnonymeUuid: localStorage.getItem('anonyme-session-token')
        });
      }
      return response;
    }),
    catchError(this.handleError)
  );
}

// You can simplify or remove the old handleGoogleSignIn and renderGoogleButton methods
// since they're now handled in the component

  renderGoogleButton(elementId: string): void {
    if (typeof google !== 'undefined' && google.accounts?.id) {
      google.accounts.id.renderButton(
        document.getElementById(elementId),
        { 
          type: 'icon',  // Changed from 'standard' to 'icon'
          shape: 'circle',  // Makes it rectangular like your button
          theme: 'filled_blue',  // Closest to your theme
          size: 'large',
          text: 'continue_with',  // Changed from 'signin_with'
          width: '100%',  // Make it full width
          logo_alignment: 'left'  // Align Google logo to left
        }
      );
      
      // // Add custom styling to the Google button
      // const googleButton = document.getElementById(elementId);
      // if (googleButton) {
      //   googleButton.style.border = '1px solid var(--primary)';
      //   googleButton.style.borderRadius = '0.375rem'; // rounded-md equivalent
      //   googleButton.style.boxShadow = '0 1px 2px 0 rgba(0, 0, 0, 0.05)'; // shadow-sm equivalent
      //   googleButton.style.padding = '0.5rem 1rem'; // py-2 px-4 equivalent
      //   googleButton.style.backgroundColor = 'var(--surface)';
      //   googleButton.style.color = 'var(--text)';
      //   googleButton.style.fontWeight = '500'; // font-medium equivalent
      //   googleButton.style.fontSize = '0.875rem'; // text-sm equivalent
      //   googleButton.style.display = 'flex';
      //   googleButton.style.alignItems = 'center';
      //   googleButton.style.justifyContent = 'center';
      //   googleButton.style.width = '100%';
      // }
      
      google.accounts.id.prompt();
    }
  }
  //#endregion

  //#region Utility Methods
  private initializeTokenRefresh() {
    const state = this.authState();
    if (state.isAuthenticated && state.expiresAt) {
      const now = Date.now();
      if (state.expiresAt > now) {
        
        // Token still valid, schedule refresh
        this.scheduleTokenRefresh(state.expiresAt);
      } else {
        // Token expired, try to refresh immediately
        this.refreshToken().subscribe();
      }
    }
  }
  refreshToken(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${environment.apiUrl}/auth/refresh`,
      {},
      { withCredentials: true }
    ).pipe(
      map(response => {
        if (response.code === '200') {
          const expiresAt = new Date(response.details?.[4]).getTime();
          this.persistAuthState({
            isAuthenticated: true,
            roles: response.details?.[1] || [],
            email:  response.details?.[0] || '',
            username: response.details?.[2] || '',
            id: response.details?.[3] || null,
            expiresAt: expiresAt
          });
          
          this.scheduleTokenRefresh(expiresAt);
        }
        return response;
      }),
      catchError(error => {
        this.clearAuthState();
        this.router.navigate(['/auth/user/signin']);
        return throwError(() => error);
      })
    );
  }
  private scheduleTokenRefresh(expiresAt: number) {
    const now = Date.now();
    const expiresIn = expiresAt - now;
    const refreshThreshold = 300000; // 5 minutes before expiration
    
    if (expiresIn > refreshThreshold) {
      
      // Schedule refresh 5 minutes before expiration
      setTimeout(() => {
        this.refreshToken().subscribe();
      }, expiresIn - refreshThreshold);
    } else if (expiresIn > 0) {
      // If less than 5 minutes remaining, refresh immediately
      this.refreshToken().subscribe();
    } else {
      // Token already expired
      this.clearAuthState();
    }
  }

  private handleError(error: any): Observable<never> {
    const errorResponse: AuthResponse = {
      code: error.error?.code?.toString() || '500',
      message: error.error?.message || 'Request failed',
      details: error.error?.details,
      
    };
    console.log("handle error here: ", error);

    return throwError(() => errorResponse);
  }
  //#endregion

  //#region Getters
  isLoggedIn(): boolean {
    return this.authState().isAuthenticated;
  }
  isAdmin(): boolean {
    return this.authState().roles.includes('ROLE_ADMIN');
  }
  get getRoles(): string[] {
    return this.normalizeRoles(this.authState().roles);
  }

  get getEmail(): string | null {
    return this.authState().email;
  }
  get getCurrentState(): AuthState {    
    return this.authState();
  }


  get getCurrentUser(): AuthState {    
    return this.authState();
  }
  get getUserId(): number | null {
    return this.authState().id;
  }
  get isAuthenticated(): boolean {
    return this.authState().isAuthenticated;
  }
  //#endregion
  private normalizeRoles(roles: string | string[]): string[] {
  const rolesArray = Array.isArray(roles) ? roles : [roles || ''];
  return rolesArray
    .flatMap(role => role.split(',').map(r => r.trim()))
    .map(role => role.replace(/[\[\]"]/g, ''))
    .filter(role => role.length > 0);
}
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
    

}
