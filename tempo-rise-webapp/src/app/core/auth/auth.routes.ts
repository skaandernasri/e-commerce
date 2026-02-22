import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  {
    path: 'user/signin',
    loadComponent: () => import('./user-signin/user-signin.component')
      .then(m => m.UserSigninComponent),
    //title: 'User Sign In'
  },
  {
    path: 'user/signup',
    loadComponent: () => import('./user-signup/user-signup.component')
      .then(m => m.UserSignupComponent)  },
  {
    path: 'admin/signin',
    loadComponent: () => import('./admin-signin/admin-signin.component')
      .then(m => m.AdminSigninComponent)  },
 
  {
    path: '',
    redirectTo: 'user/signin',
    pathMatch: 'full'
  }
];