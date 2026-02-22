import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, switchMap, take } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

// Role constants for better maintainability
const ROLE_ADMIN = 'ROLE_ADMIN';
const ROLE_SUPER_ADMIN = 'ROLE_SUPER_ADMIN';
const ROLE_CLIENT = 'ROLE_CLIENT';
const ROLE_REDACTEUR = 'ROLE_REDACTEUR';
const ROLE_GESTIONNAIRE = 'ROLE_GESTIONNAIRE';

// Main guard factory function
const createAuthGuard = (requiredRoles: string[], isAdminRoute: boolean = false): CanActivateFn => {
  return (route, state): Observable<boolean | UrlTree> => {
    const authService = inject(AuthService);
    const router = inject(Router);
    return authService.authState().isAuthenticated ? checkRoles(authService, requiredRoles, router, state):
      of(createSignInUrlTree(router, state, isAdminRoute));
  };
};
export const authGuard= createAuthGuard([ROLE_CLIENT,ROLE_SUPER_ADMIN,ROLE_ADMIN,ROLE_REDACTEUR,ROLE_GESTIONNAIRE])
export const authClientGuard = createAuthGuard([ROLE_CLIENT]);
export const authAdminGuard = createAuthGuard([ROLE_ADMIN, ROLE_SUPER_ADMIN], true);
export const authSuperAdminGuard = createAuthGuard([ROLE_SUPER_ADMIN], true);
export const authRedacteurGuard = createAuthGuard([ROLE_REDACTEUR], true);
export const authGestionnaireGuard = createAuthGuard([ROLE_GESTIONNAIRE], true);
function createSignInUrlTree(router: Router, state: any, isAdmin: boolean = false): UrlTree {
  const path = isAdmin ? '/auth/admin/signin' : '/auth/user/signin';
  return router.createUrlTree([path], {
    queryParams: { returnUrl: state.url }
  });
}

function checkRoles(
  authService = inject(AuthService),
  requiredRoles: string[],
  router: Router,
  state: any,
): Observable<boolean | UrlTree> {
  const userRoles = authService.getRoles;
  
  //const userRolesNormalized = normalizeRoles(userRoles);
  //console.log("normalized"+ userRolesNormalized);
  
  const hasAccess = requiredRoles.some(role => userRoles.includes(role));  
  if (hasAccess) {
    return of(true);
  }

  const isAdminRoute = requiredRoles.some(role => [ROLE_ADMIN, ROLE_SUPER_ADMIN,ROLE_GESTIONNAIRE].includes(role));
  
  return authService.logout().pipe(
    map(() => createSignInUrlTree(router, state, isAdminRoute)),
    catchError(() => of(createSignInUrlTree(router, state, isAdminRoute))),
    switchMap(urlTree => of(urlTree))
  );
}