// auth.interceptor.ts
import { Injectable, Injector } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
  HttpResponse
} from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { SuiviClientService } from '../services/suivi-client.service';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';
import { CartService } from '../services/cart.service';
import { VariantService } from '../services/variant.service';
import { Router } from '@angular/router';
import { UtilisateurAnonymeService } from '../services/utilisateur-anonyme.service';

@Injectable(
  {providedIn: 'root'}
)
export class AuthInterceptor implements HttpInterceptor {

  baseUrl=environment.apiUrl
  constructor(private injector: Injector,private router : Router) { }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const authService = this.injector.get(AuthService); // ✅ Lazy injection
        const suiviClientService = this.injector.get(SuiviClientService); // ✅ Lazy injection
        const cartService = this.injector.get(CartService); // ✅ Lazy injection
        const variantService = this.injector.get(VariantService);
        const utilisaterAnonymeService = this.injector.get(UtilisateurAnonymeService);

    const authReq = req.clone({
      withCredentials: true
    });
    
    return next.handle(authReq).pipe(
      tap((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          const url = event.url ?? '';
          const method = req.method;
          const userId = authService.authState().isAuthenticated ? authService.authState().id:null;
          
          const sessionToken = utilisaterAnonymeService.sessionToken();         
          
switch (true) {
  case /\/v1\/produits\/\d+$/.test(url):
    {
      
      // Product view URL
      const productId = Number(url.match(/\/produits\/(\d+)$/)![1]);
      if (productId) {
        suiviClientService.addAction({
          utilisateurId: userId,
          produitId: productId,
          typeAction: "VIEW_PRODUCT",
          utilisateurAnonymeUuid: sessionToken
        });
      }
    }
    break;

  case /\/utilisateur\/\d+\/panier\/add-item\/\d+/.test(url):
    {
      // Add item to cart
      const match = url.match(/\/utilisateur\/(\d+)\/panier\/add-item\/(\d+)/)!;
      const variantId = Number(match[2]);
      variantService.getVariantById(variantId).subscribe(variant => {
        
        if (variant && variant.productId) {
          const productId = variant.productId;
          
          suiviClientService.addAction({
            utilisateurId: userId,
            produitId: productId,
            typeAction: "ADD_TO_CART",
            utilisateurAnonymeUuid: sessionToken
          });
        }
      });
    }
    break;
      case method === 'PUT' && /\/utilisateur\/\d+\/panier\/items\/\d+/.test(url) :
    {
      // Update item in cart
      const match = url.match(/\/utilisateur\/(\d+)\/panier\/items\/(\d+)/)!;
      const variantId = Number(match[2]);

      variantService.getVariantById(variantId).subscribe(variant => {
        
        if (variant && variant.productId) {
          const productId = variant.productId;
          
          suiviClientService.addAction({
            utilisateurId: userId,
            produitId: productId,
            typeAction: "ADD_TO_CART",
            utilisateurAnonymeUuid: sessionToken
          });
        }
      });
      
    }
    break;
  case /\/utilisateur\/\d+\/panier\/items\/\d+/.test(url):
    {
      // Remove item from cart
      const match = url.match(/\/utilisateur\/(\d+)\/panier\/items\/(\d+)/)!;
      const variantId = Number(match[2]);

      variantService.getVariantById(variantId).subscribe(variant => {
        
        if (variant && variant.productId) {
          const productId = variant.productId;
          
          suiviClientService.addAction({
            utilisateurId: userId,
            produitId: productId,
            typeAction: "REMOVE_FROM_CART",
            utilisateurAnonymeUuid: sessionToken
          });
        }
      });
    }
    break;
    case /\/utilisateur\/\d+\/panier\/\d+$/.test(url): {
      //remove all items from cart
  const match = url.match(/\/utilisateur\/(\d+)\/panier\/(\d+)$/)!;

  const cart = cartService.cart(); // get current cart state from signal
      
  for (const item of cart.articles) {
    suiviClientService.addAction({
      utilisateurId: userId,
      produitId: item.id,
      typeAction: "REMOVE_FROM_CART",
      utilisateurAnonymeUuid: sessionToken
    });
  }
}
break;
case method === 'POST' && /\/commandes/.test(url): {
  const body = req.body;
  const userId = body?.utilisateurId;

  if (!userId) {
    console.warn("utilisateurId not found in request body");
    break;
  }

  const cart = cartService.cart(); // signal or behavior subject

  for (const item of cart.articles) {
    suiviClientService.addAction({
      utilisateurId: userId,
      produitId: item.id,
      typeAction: "PURCHASE",
      utilisateurAnonymeUuid: sessionToken
    });
  }
}
break;
case method === 'POST' && /\/avis/.test(url): {
  const body = req.body;
  const userId = body?.utilisateurId;
  const itemId=body?.produitId;

  if (!userId) {
    console.warn("utilisateurId not found in request body");
    break;
  }
   suiviClientService.addAction({
      utilisateurId: userId,
      produitId: itemId,
      typeAction: "LEAVE_REVIEW"
    })

}
break;



  // Add more cases here for other URL patterns

  default:
    // No match, do nothing or handle default case
    break;
}

        }
      }),
      
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 || error.status === 403) {
          // Handle unauthorized errors
          this.router.navigateByUrl('/unauthorized');
          console.error('Auth error:', error);
          // Optionally redirect to login
        }
        return throwError(() => error);
      })
    );
  }
}