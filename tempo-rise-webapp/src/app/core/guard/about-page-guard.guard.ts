import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ParamsService } from '../services/params.service';
import { catchError, map, of } from 'rxjs';

export const aboutPageGuardGuard: CanActivateFn = (route, state) => {
  const sectionsService = inject(ParamsService);
  const router = inject(Router);
  return sectionsService.getSectionsByPageType('ABOUT').pipe(
      map(sections => {
        const activeSections = sections.filter(section => section.active);
        if (!activeSections.length) {
          
          return router.createUrlTree(['/not-found']);;
        }
        return true;
      }),
      catchError((error) => {
        console.error('Error loading sections:', error);
        router.createUrlTree(['/not-found']);
        return of(false);
      })
    );
};
