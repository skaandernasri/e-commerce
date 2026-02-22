import { Component, effect, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import {TranslateService} from "@ngx-translate/core";
import { LanguageService } from './core/services/language.service';
import { AuthService } from './core/services/auth.service';
import { ThemeService } from './core/services/theme.service';
import { UtilisateurAnonymeService } from './core/services/utilisateur-anonyme.service';
import { filter } from 'rxjs';
import { WebSocketService } from './core/services/web-socket.service';
import { NotifService } from './core/services/notif.service';
import { environment } from '../environments/environment';
import { CookiesConsentService } from './core/services/cookies.service';
import { MetaPixelService } from './core/services/meta-pixel.service';
declare global {
  interface Window {
    gtag: (...args: any[]) => void;
  }
}
@Component({
    selector: 'app-root',
    imports: [CommonModule, RouterOutlet],
    template: `
    <router-outlet></router-outlet>
  `,
  providers: [LanguageService,TranslateService]
  
})
export class AppComponent {
   visible = signal(false);
  private authService=inject(AuthService)
  private cookieService=inject(CookiesConsentService)
  private metaPixelService=inject(MetaPixelService)
  constructor(
    private languageService: LanguageService,
    private themeService: ThemeService,
    private utilisateurAnonymeService: UtilisateurAnonymeService,
    private router: Router,
    private notifService: NotifService,
  private webSocketService:WebSocketService) {
    effect(() => {
       document.documentElement.classList.remove('theme-light','theme-dark', 'theme-colorful');
            if (this.themeService.currentTheme$() !== 'light') {
        document.documentElement.classList.add(`theme-${this.themeService.currentTheme$()}`);
      }
    });
    effect(() =>{
      if(this.authService.authState().isAuthenticated){
        this.webSocketService.connect();
        this.notifService.getAllNotifs(this.authService.authState().id!,1).subscribe();
      }
      else{
        this.notifService.resetAllSignals();
        this.webSocketService.disconnect();
      }
      
    });
  }
  ngOnInit() {
      this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        // Scroll to the top of the page
        window.scrollTo({ top: 0, behavior: 'smooth' });
        if(environment.production){
          window.gtag?.('config', 'G-W1MYJNM8CE', { 'page_path': this.router.url });
          }
      });
    this.cookieService.getOrCreateCookieConsent(null).subscribe();
      this.utilisateurAnonymeService
    .getOrCreateUtilisateurAnonyme({})
    .subscribe();    

    if (this.authService.authState().isAuthenticated) {
      this.authService.checkAuthStatus();
    }
    //this.titleService.setTitle($localize`:@@title:ecommerce-site`);
    this.languageService.initializeLanguage();
  // Translate and set the title
    // this.translate.get('TITLE').subscribe((res: string) => {
    //   this.titleService.setTitle(res); // Set the translated title
    // });

    // Update the title when the language changes
    // this.translate.onLangChange.subscribe(() => {
    //   this.translate.get('TITLE').subscribe((res: string) => {
    //     this.titleService.setTitle(res); // Update the title
    //   });
    // });
  }
  }
