import { Routes } from '@angular/router';
import { authAdminGuard } from '../../core/auth/auth.guard';
import { N } from '@angular/cdk/overlay.d-BdoMy0hX';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./layouts/admin-layout/admin-layout.component')
      .then(m => m.AdminLayoutComponent),
    canActivate: [authAdminGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component')
          .then(m => m.DashboardComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./users-management/users-management.component')
          .then(m => m.UsersManagementComponent)
      },
      {
        path: 'orders',
        loadComponent: () => import('./orders-management/orders-management.component')
          .then(m => m.OrdersManagementComponent)
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./orders-management/order-details/order-details.component')
          .then(m => m.OrderDetailsComponent)
      },
      {
        path: 'products',
        loadComponent: () => import('./product-management/products-management.component')
          .then(m => m.ViewProductsComponent)
      },
      {
        path: 'reviews',
        loadComponent: () => import('./reviews-management/reviews-management.component')
          .then(m => m.ReviewsManagementComponent)
      },
      {
        path: 'invoices',
        loadComponent: () => import('./invoices-management/invoices-management.component')
          .then(m => m.InvoicesManagementComponent)
      },
      {
        path: 'invoice/:id',
        loadComponent: () => import('./invoices-management/invoice-details/invoice-details.component')
          .then(m => m.InvoiceDetailsComponent)
      },
      {
        path: 'categories',
        loadComponent: () => import('./category-management/categories-management.component')
          .then(m => m.ViewCategoriesComponent)
      },
      {
        path: 'promotions',
        loadComponent: () => import('./promotions-management/promotions-management.component')
          .then(m => m.PromotionsManagementComponent)
      },
      {
        path: 'codesPromo',
        loadComponent: () => import('./code-promo-management/code-promo-management.component')
          .then(m => m.CodePromoManagementComponent)
      },
      {
        path: 'blog',
        loadComponent: () => import('./blog-admin/blog-admin.component')
          .then(m => m.BlogAdminComponent)
      },
      {
        path: 'addblog',
        loadComponent: () => import('./blog-admin/add-blog/add-blog.component')
          .then(m => m.AddBlogComponent)
      },
      {
        path: 'updateblog/:id',
        loadComponent: () => import('./blog-admin/update-blog/update-blog.component')
          .then(m => m.UpdateBlogComponent)
      },
      {
        path: 'contacts',
        loadComponent: () => import('./contact-management/contact-management.component')
          .then(m => m.ContactManagementComponent)
      },
      {
        path: 'recommandation-system',
        loadComponent: () => import('./recommendation-model-management/recommendation-model-management.component')
          .then(m => m.RecommendationModelManagementComponent)
      },
      {
        path: 'params/sections',
        loadComponent: () => import('./param-management/param-management.component')
          .then(m => m.ParamManagementComponent)
      },
      {
        path: 'params',
        loadComponent: () => import('./param-management/global-params/global-params.component')
          .then(m => m.GlobalParamsComponent)
      }
    ]
  }
];
