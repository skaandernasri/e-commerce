import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
  path: 'auth',
    loadChildren: () => import('./core/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'reset-request',
    loadComponent: () => import('./core/auth/password-reset/reset-request/reset-request.component').then(m => m.ResetRequestComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./core/auth/password-reset/password-reset.component').then(m => m.PasswordResetComponent),
    title: 'Reset Password'
  },
    {
    path: 'activate'
    ,loadComponent: () => import('./core/auth/activate-account/activate-account.component').then(m => m.ActivateAccountComponent)
  },
  {
    path: '',
    loadChildren: () => import('./features/user/user.routes').then(m => m.USER_ROUTES)
  },
  {
    path: 'not-found',
    loadComponent: () => import('./shared/components/exceptions/not-found/not-found.component').then(m => m.NotFoundComponent)
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./shared/components/exceptions/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
  },
  {
    path: '**',
    redirectTo: '/not-found'
  }
];