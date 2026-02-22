import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MainHeaderComponent } from '../../../../shared/components/main-header/main-header.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { CartComponent } from '../../cart/cart.component';
import { Subscription } from 'rxjs';
import { animate, style, transition, trigger } from '@angular/animations';
import { CookiesConsentService } from '../../../../core/services/cookies.service';
import { SocialLinksComponent } from "../../../../shared/components/social-links/social-links.component";

function getCssVar(varName: string): string {
  return getComputedStyle(document.documentElement)
    .getPropertyValue(varName)
    .trim();
}

@Component({
  selector: 'app-user-layout',
  imports: [RouterOutlet, MainHeaderComponent, FooterComponent, CartComponent, SocialLinksComponent],
  templateUrl: './user-layout.component.html',
  animations: [
    trigger('fade', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('500ms ease-in-out', style({ opacity: 1 })),
      ]),
      transition(':leave', [
        style({ opacity: 1 }),
        animate('500ms ease-in-out', style({ opacity: 0 })),
      ]),
    ]),
  ]
})
export class UserLayoutComponent implements OnInit, OnDestroy {
  private popupOpenSubscription!: Subscription;
  private popupCloseSubscription!: Subscription;
  private initializingSubscription!: Subscription;
  private initializedSubscription!: Subscription;
  private initializationErrorSubscription!: Subscription;
  private statusChangeSubscription!: Subscription;
  private revokeChoiceSubscription!: Subscription;
  private noCookieLawSubscription!: Subscription;

  private hasCheckedConsent = false;
    cookiesService = inject(CookiesConsentService)
    cookieExists = this.cookiesService.cookieExists$
    accept() {
      this.cookiesService.accept();
    }
    decline() {
      this.cookiesService.decline();
    }
  //isVisible = signal(false);
// private ccService: NgcCookieConsentService

  ngOnInit() {
    // Subscribe to events BEFORE checking status
    // this.open();
    // this.close();
    // this.initializing();
    // this.initialized();
    // this.initializationError();
    // this.statusChange();
    // this.revokeChoice();
    // this.noCookieLaw();
    //     setTimeout(() => {
    //   this.checkCookieConsent();
    // }, 100);
    //this.checkCookieConsent();
  }

  // updateCookieText() {
  //   const config = this.ccService.getConfig();
        
  //   // Update configuration
  //   if (config.cookie) {
  //     config.cookie.domain = environment.domain;
  //     config.cookie.name = 'cookieconsent_status';
  //   }

  //   if (config.palette) {
  //     if(config.palette.popup) config.palette.popup.background = getCssVar('--background');
  //     if(config.palette.button) config.palette.button.background = getCssVar('--primary');
  //     if(config.palette.button) config.palette.button.text = getCssVar('--text2');
  //   }

  //   if (config.content) {
  //     config.content.message = 'We use cookies to improve your experience, analyze traffic, and for marketing purposes. You can accept or decline these cookies at any time.';
  //     config.content.allow = 'Accept';
  //     config.content.deny = 'Decline';
  //     config.content.link = ''; // hide link
  //     config.content.href = ''; // remove URL
  //   }
    
  //   // Reinitialize with updated config
  //   this.ccService.destroy();
  //   this.ccService.init(config);
  // }

  // open() {
  //   this.popupOpenSubscription = this.ccService.popupOpen$.subscribe(() => {});
  // }

  // close() {
  //   this.popupCloseSubscription = this.ccService.popupClose$.subscribe(() => {});
  // }

  // initializing() {
  //   this.initializingSubscription = this.ccService.initializing$.subscribe(
  //     (event: NgcInitializingEvent) => {
  //       console.log(`initializing: ${JSON.stringify(event)}`);
  //     }
  //   );
  // }

  // initialized() {
  //   this.initializedSubscription = this.ccService.initialized$.subscribe(() => {
  //     console.log('Cookie consent initialized via subscription');
  //     this.checkCookieConsent();
  //   });
  // }

  // checkCookieConsent() {
  //   // Prevent infinite loop - only check once
  //   if (this.hasCheckedConsent) {
  //     console.log('Already checked consent, skipping...');
  //     return;
  //   }
    
  //   this.hasCheckedConsent = true;
  //   console.log('Checking cookie consent...');
  //   const hasConsent = localStorage.getItem('cookie_consent');
  //   console.log('Has consent in localStorage:', hasConsent);
    
  //   if (hasConsent) {
  //     // User has already made a choice, hide the popup
  //     console.log("User already consented, destroying popup");
  //     this.isVisible.set(false);
  //     try {
  //       const config = this.ccService.getConfig();
  //       if (config) {
  //         this.ccService.destroy();
  //         console.log("Cookie consent destroyed");
  //       }
  //     } catch (error) {
        
  //     }
  //   } else {
  //     // First time visitor, update config and show popup
  //     console.log("First time visitor, updating cookie text");
  //     this.updateCookieText();
  //     this.isVisible.set(true);
  //   }
  // }

  // initializationError() {
  //   this.initializationErrorSubscription = this.ccService.initializationError$.subscribe(
  //     (event: NgcInitializationErrorEvent) => {
  //       console.log(`initializationError: ${JSON.stringify(event.error?.message)}`);
  //     }
  //   );
  // }

  // statusChange() {
  //   this.statusChangeSubscription = this.ccService.statusChange$.subscribe(
  //     (event: NgcStatusChangeEvent) => {
  //       if (event.status === 'allow') {
  //         localStorage.setItem('cookie_consent', 'allow');
  //       }
  //       if (event.status === 'deny') {
  //         localStorage.setItem('cookie_consent', 'deny');
  //       }
  //       this.isVisible.set(false);
  //     }
  //   );
  // }

  // revokeChoice() {
  //   this.revokeChoiceSubscription = this.ccService.revokeChoice$.subscribe(() => {});
  // }

  // noCookieLaw() {
  //   this.noCookieLawSubscription = this.ccService.noCookieLaw$.subscribe(
  //     (event: NgcNoCookieLawEvent) => {}
  //   );
  // }

  ngOnDestroy() {
    // this.popupOpenSubscription.unsubscribe();
    // this.popupCloseSubscription.unsubscribe();
    // this.initializingSubscription.unsubscribe();
    // this.initializedSubscription.unsubscribe();
    // this.initializationErrorSubscription.unsubscribe();
    // this.statusChangeSubscription.unsubscribe();
    // this.revokeChoiceSubscription.unsubscribe();
    // this.noCookieLawSubscription.unsubscribe();
  }
}