import { Routes } from '@angular/router';
import { authClientGuard, authGuard } from '../../core/auth/auth.guard';
import { productGuardGuard } from '../../core/guard/product-guard.guard';
import { homeGuardGuard } from '../../core/guard/home-guard.guard';
import { aboutPageGuardGuard } from '../../core/guard/about-page-guard.guard';
import { tacPageGuard } from '../../core/guard/tac-page.guard';
import { productsPageGuardGuard } from '../../core/guard/products-page-guard.guard';
import { blogsPageGuard } from '../../core/guard/blogs-page.guard';
import { blogGuard } from '../../core/guard/blog.guard';
import { deliveryAndReturnPageGuard } from '../../core/guard/delivery-and-return-page.guard';
import { legalNoticePageGuard } from '../../core/guard/legal-notice-page.guard';

export const USER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./layouts/user-layout/user-layout.component')
      .then(m => m.UserLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./home/home.component')
          .then(m => m.HomeComponent),
          canActivate:[homeGuardGuard]
      },
      {
        path: 'products',
        loadComponent: () => import('./products/products.component')
          .then(m => m.ProductsComponent),
          canActivate:[productsPageGuardGuard]
      },
      {
        path: 'product/:id',
        loadComponent: () => import('./products/product-details/product-details.component')
          .then(m => m.ProductDetailsComponent),
          canActivate:[productGuardGuard]
      },
      {
        path: 'cart',
        loadComponent: () => import('./cart/cart.component')
          .then(m => m.CartComponent)
      },
      {
        path: 'checkout',
        loadComponent: () => import('./checkout/checkout.component')
          .then(m => m.CheckoutComponent),
          //canActivate: [authClientGuard],
      },
      {
        path: 'profile',
        loadComponent: () => import('./profile/profile.component')
          .then(m => m.ProfileComponent),
          canActivate: [authGuard],
      },
      {
        path: 'blog',
        loadComponent: () => import('./blog/blog.component')
          .then(m => m.BlogComponent),
          canActivate:[blogsPageGuard]
      },
      {
        path: 'blog/:id',
        loadComponent: () => import('./blog/blog-post/blog-post.component')
          .then(m => m.BlogPostComponent),
          canActivate:[blogGuard]
      },
      {
        path: 'contact',
        loadComponent: () => import('./contact/contact.component')
          .then(m => m.ContactComponent)
      },
      {
        path: 'about',
        loadComponent: () => import('./about/about.component')
          .then(m => m.AboutComponent),
          canActivate:[aboutPageGuardGuard]
      },
      {
        path: 'terms-and-conditions',
        loadComponent: () => import('./tac/tac.component')
          .then(m => m.TacComponent),
          canActivate:[tacPageGuard]
      },
      {
        path: 'delivery-and-return',
        loadComponent: () => import('./delivery-and-return/delivery-and-return.component')
          .then(m => m.DeliveryAndReturnComponent),
          canActivate:[deliveryAndReturnPageGuard]
      },
      {
        path: 'legal-notice',
        loadComponent: () => import('./legal-notice/legal-notice.component')
          .then(m => m.LegalNoticeComponent),
          canActivate:[legalNoticePageGuard]
      },
      {
        path: 'whichlist',
        loadComponent: () => import('./whishlist/whishlist.component')
          .then(m => m.WhishlistComponent),
          canActivate: [authClientGuard],
      },
      {
        path: 'paymentDetails/:id',
        loadComponent: () => import('./payment/details/details.component')
          .then(m => m.DetailsComponent),
          //canActivate: [authClientGuard],
      }
    ],
  },
    {
    path: 'payment-success',
    loadComponent: () => import('./payment/success/success.component')
      .then(m => m.SuccessComponent),
    //canActivate: [authClientGuard],
  },
  {
    path: 'payment-failure',
    loadComponent: () => import('./payment/fail/fail.component')
      .then(m => m.FailComponent),
   // canActivate: [authClientGuard],
  },
];