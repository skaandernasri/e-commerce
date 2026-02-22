import { ApplicationConfig,importProvidersFrom,provideZoneChangeDetection } from '@angular/core';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { TranslateLoader,TranslateModule,provideTranslateService, } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient } from '@angular/common/http';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { routes } from './app.routes';
import { SocialAuthServiceConfig } from '@abacritt/angularx-social-login';
import { AuthInterceptor } from './core/auth/auth.interceptor';
import {GoogleLoginProvider,} from '@abacritt/angularx-social-login';
import { environment } from '../environments/environment';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async'; // Add this
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { provideNgcCookieConsent } from 'ngx-cookieconsent';
import { cookieConfig } from './shared/configs/cookieConfig';

// Translation loader function
const httpLoaderFactory: (http: HttpClient) => TranslateHttpLoader = (http: HttpClient) =>
  new TranslateHttpLoader(http, './assets/i18n/', '.json');
 
export const appConfig: ApplicationConfig = {
    providers: [  
      //provideNgcCookieConsent(cookieConfig),
      provideAnimations(), // required animations providers
      provideToastr({
      positionClass: 'toast-top-center',
      timeOut: 3000,
      progressBar: true,
      closeButton: true,
      preventDuplicates: true
      }), // Toastr providers
      provideAnimationsAsync(),
      provideRouter(routes,withInMemoryScrolling({
        anchorScrolling: 'enabled',
        scrollPositionRestoration: 'enabled'
      })),
      provideZoneChangeDetection({ eventCoalescing: true }),
      provideHttpClient( withInterceptorsFromDi()),
      {
        provide: HTTP_INTERCEPTORS,
        useClass: AuthInterceptor,
        multi: true
      },

      {
        provide: 'SocialAuthServiceConfig',
        useValue: {
          autoLogin: false,
          providers: [
            {
              id: GoogleLoginProvider.PROVIDER_ID,
              provider: new GoogleLoginProvider(
                environment.googleClientId,
                {
                  scopes: ['profile', 'email']
                }
              )
              
            }
          ],
          onError: (err) => {
            console.error('SocialAuthServiceConfig error:', err);
          }
        } as SocialAuthServiceConfig,
      },
        provideTranslateService({
        loader: {
          provide: TranslateLoader,
          useFactory: httpLoaderFactory,
          deps: [HttpClient],
        },
      }),

    ],
  };