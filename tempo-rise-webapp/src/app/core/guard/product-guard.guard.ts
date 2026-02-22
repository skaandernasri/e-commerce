import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ProductService } from '../services/product.service';
import { catchError, forkJoin, map, of } from 'rxjs';
import { ParamsService } from '../services/params.service';

export const productGuardGuard: CanActivateFn = (route, state) => {
  const productService = inject(ProductService);
  const sectionsService = inject(ParamsService);
  const router = inject(Router);
  const productId = route.params['id'];
  if(productId){
    const product$ = productService.getProductById(productId).pipe(
      map(product => {
        if (product.actif === true) {
          return true;
        } else {
          return false;
        }
      })
    );
    const productsSection$ = sectionsService.getSectionsByPageType('PRODUCT').pipe(
      map(sections => {
        const activeSections = sections.filter(section => section.active);
        if (!activeSections.length) {
          return false;
        }
        return true;
      })
    )
    return forkJoin([product$, productsSection$]).pipe(
      map(([v1, v2]) => {
        if(v1===true && v2===true){
          return true;
        }
        else{
          return router.createUrlTree(['/not-found']);
        }
      }),
      catchError(() => {
        router.navigate(['/not-found']);
        return of(false);
      })
    )
  }

  return true;
};
