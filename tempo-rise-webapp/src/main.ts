import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { appConfig } from './app/app.config';
import './polyfills';
import { environment } from './environments/environment';

const script = document.createElement('script');
script.src = `https://www.google.com/recaptcha/api.js?render=${environment.recaptchaSiteKey}&onload=onloadCallback&hl=en`;
script.async = true;
script.defer = true;
document.head.appendChild(script);
bootstrapApplication(AppComponent, {
  providers: [
    ...appConfig.providers, 
    provideRouter(routes),
  ],
}).catch((err) => console.error(err));