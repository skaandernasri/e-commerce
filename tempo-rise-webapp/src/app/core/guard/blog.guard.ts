import { CanActivateFn, Router } from '@angular/router';
import { BlogService } from '../services/blog.service';
import { inject } from '@angular/core';
import { ParamsService } from '../services/params.service';
import { catchError, forkJoin, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const blogGuard: CanActivateFn = (route, state) => {
   const blogService = inject(BlogService);
  const sectionsService = inject(ParamsService);
  const authService = inject(AuthService);
  const router = inject(Router);
  const blogId = route.params['id'];
  if(blogId){
    const blog$ = blogService.getBlogById(blogId).pipe(
      map(blog => {
        if (blog.status === 'PUBLIER') {
          return true;
        } else {
          return false;
        }
      })
    );
    const blogsSection$ = sectionsService.getSectionsByPageType('BLOG').pipe(
      map(sections => {
        const activeSections = sections.filter(section => section.active);
        if (!activeSections.length) {
          return false;
        }
        return true;
      })
    )
    return forkJoin([blog$, blogsSection$]).pipe(
      map(([v1, v2]) => {
        if(authService.authState().isAuthenticated){
          if(authService.getRoles.includes('ROLE_GESTIONNAIRE') || authService.getRoles.includes('ROLE_ADMIN')){return true;}
          else{
            return router.createUrlTree(['/not-found']);
          }
        }
        else if(v1===true && v2===true){
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
