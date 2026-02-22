import { Injectable, Inject, PLATFORM_ID, Optional } from '@angular/core';
import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { contentType, currency, MetaPixelEventRequest } from '../models/metaPixelEvent';
import { v4 as uuidv4 } from 'uuid';
import { HttpClient } from '@angular/common/http';

// Extend the Window interface for TS support
declare global {
  interface Window {
    fbq?: (...args: any[]) => void;
  }
}

// Type Definitions

export interface Contents {
  id: string;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class MetaPixelService {
  private isProd = environment.production;
  private pixelId = environment.pixelId;
  private isBrowser: boolean;
  private sendMetaPixelEventUrl = `${environment.apiUrl}/meta/pixel/event`

  constructor(
    @Inject(PLATFORM_ID) platformId: Object,
    @Inject(DOCUMENT) private document: Document,
    @Optional() private router: Router,
    private http: HttpClient
  ) {
    this.isBrowser = isPlatformBrowser(platformId);

    // Only run initialization in the browser
    if (this.isBrowser && this.pixelId) {
      this.initPixel();
      
      // Auto-track PageViews
      if (this.router) {
        this.router.events.pipe(
          filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
          this.addPageViewEvent();
        });
      }
    } else if (!this.pixelId) {
      console.warn('Meta Pixel ID not found in environment configuration.');
    }
  }

  // ===========================
  // Initialization (The Snippet)
  // ===========================
private initPixel() {
    // Added '?' to n, t, and s to make them optional
    (function(f: any, b: any, e: any, v: any, n?: any, t?: any, s?: any) {
      if (f.fbq) return;
      n = f.fbq = function() {
        n.callMethod ? n.callMethod.apply(n, arguments) : n.queue.push(arguments);
      };
      if (!f._fbq) f._fbq = n;
      n.push = n;
      n.loaded = true;
      n.version = '2.0';
      n.queue = [];
      
      t = b.createElement(e);
      t.async = true;
      t.src = v;
      
      s = b.getElementsByTagName(e)[0];
      s.parentNode.insertBefore(t, s);
    })(window, document, 'script', 'https://connect.facebook.net/en_US/fbevents.js');

    if (window.fbq) {
      window.fbq('init', this.pixelId);
    }
  }

  // ===========================
  // Core safe tracker
  // ===========================
  /**
   * Safe track method with retry limit to prevent infinite loops 
   * caused by Ad Blockers.
   */
  private track(event: string, data?: any, eventId?: string, retryCount = 0) {
    if (!this.isBrowser || !this.isProd ) {
      if (!this.isProd) console.log(`[Dev Mode] Pixel Event: ${event}`, data);
      return;
    }

    // Stop retrying after 3 seconds (6 attempts * 500ms) to prevent memory leaks
    if (retryCount > 6) {
      console.warn('Meta Pixel could not be loaded (likely blocked by client).');
      return;
    }

    // Add deduplication ID if provided
    if (eventId) {
      data = { ...data, event_id: eventId };
      console.log(event," event id in track", eventId);
      
    }

    if (typeof window.fbq === 'function') {
      window.fbq('track', event, data);
    } else {
      setTimeout(() => this.track(event, data, eventId, retryCount + 1), 500);
    }
  }

  // ===========================
  // PageView
  // ===========================
  addPageViewEvent() {
    const eventId = uuidv4();    
    this.track('PageView', {
      content_name: this.isBrowser ? window.location.href : ''
    },eventId
  );
  const fbp = this.getCookie("fbp");
  const fbc = this.getCookie("fbc");
    const request:MetaPixelEventRequest = {
      data: [
        {
          event_name: 'PageView',
          event_time: this.getEpochTime(),
          event_id: eventId,
          action_source: 'website',
          event_source_url: this.isBrowser ? window.location.href : '',
          user_data: {
            client_user_agent: navigator.userAgent,
            ...(fbp ? {fbp: fbp } : {}),
            ...(fbc ? {fbc: fbc } : {})
          }
        }
      ]
    };    
    this.sendMetaPixelEvent(request);
    
  }

  // ===========================
  // ViewContent
  // ===========================
  addViewContentEvent(
    content_ids: string[],
    content_type: contentType,
    contents: Contents[],
    currency: currency,
    value: number,
  ) {
    const eventId = uuidv4();
    this.track(
      'ViewContent',
      { 
        content_name: this.isBrowser ? window.location.href : '', 
        content_ids, 
        content_type, 
        contents, 
        currency, 
        value 
      },
      eventId
    );

    const fbp = this.getCookie("fbp");
    const fbc = this.getCookie("fbc");

    const request:MetaPixelEventRequest = {
      data: [
        {
          event_name: 'ViewContent',
          event_time: this.getEpochTime(),
          event_id: eventId,
          action_source: 'website',
          event_source_url: this.isBrowser ? window.location.href : '',
          user_data: {
            client_user_agent: navigator.userAgent,
            ...(fbp ? {fbp: fbp } : {}),
            ...(fbc ? {fbc: fbc } : {})
          },
          custom_data: {
            content_ids,
            content_type,
            contents,
            currency,
            value
          }
        }
      ]
    };
    this.sendMetaPixelEvent(request);
  }

  // ===========================
  // AddToCart
  // ===========================
  addAddToCartEvent(
    content_ids: string[],
    content_type: contentType,
    contents: Contents[],
    currency: currency,
    value: number,
  ) {
    const eventId = uuidv4();
    this.track(
      'AddToCart',
      { content_ids, content_type, contents, currency, value },
      eventId
    );
    const fbp = this.getCookie("fbp");
    const fbc = this.getCookie("fbc");

    const request:MetaPixelEventRequest = {
      data: [
        {
          event_name: 'AddToCart',
          event_time: this.getEpochTime(),
          event_id: eventId,
          action_source: 'website',
          event_source_url: this.isBrowser ? window.location.href : '',
          user_data: {
            client_user_agent: navigator.userAgent,
            ...(fbp ? {fbp: fbp } : {}),
            ...(fbc ? {fbc: fbc } : {})
          },
          custom_data: {
            content_ids,
            content_type,
            contents,
            currency,
            value
          }
        }
      ]
    };
    this.sendMetaPixelEvent(request);
  }

  // ===========================
  // InitiateCheckout
  // ===========================
  addInitiateCheckoutEvent(
    content_ids: string[],
    contents: Contents[],
    currency: currency,
    num_items: number,
    value: number  
  ) {
    const eventId = uuidv4();
    this.track(
      'InitiateCheckout',
      { content_ids, contents, currency, num_items, value },
      eventId
    );
    const fbp = this.getCookie("fbp");
    const fbc = this.getCookie("fbc");

    const request:MetaPixelEventRequest = {
      data: [
        {
          event_name: 'InitiateCheckout',
          event_time: this.getEpochTime(),
          event_id: eventId,
          action_source: 'website',
          event_source_url: this.isBrowser ? window.location.href : '',
          user_data: {
            client_user_agent: navigator.userAgent,
            ...(fbp ? {fbp: fbp } : {}),
            ...(fbc ? {fbc: fbc } : {})
          },
          custom_data: {
            content_ids,
            contents,
            currency,
            num_items,
            value
          }
        }
      ]
    };
    this.sendMetaPixelEvent(request);
  }

  // ===========================
  // Lead
  // ===========================
  async addLeadEvent(content_name: string, email?:string) {
    const eventId = uuidv4();
    const hashedEmail = email ? await this.hashSHA256(email) : '';
    this.track('Lead', { content_name, ...(email ? {user_data: {em:[hashedEmail]}} : {})  }, eventId);
    const fbp = this.getCookie("fbp");
    const fbc = this.getCookie("fbc");
    const request:MetaPixelEventRequest = {
      data: [
        {
          event_name: 'Lead',
          event_time: this.getEpochTime(),
          event_id: eventId,
          action_source: 'website',
          event_source_url: this.isBrowser ? window.location.href : '',
          user_data: {
            ...(email ? {em:[hashedEmail]} : {}),
            client_user_agent: navigator.userAgent,
            ...(fbp ? {fbp: fbp } : {}),
            ...(fbc ? {fbc: fbc } : {})
          },
          custom_data: {
            content_name
          }
        }
      ]
    };
    this.sendMetaPixelEvent(request);
  }

  // ===========================
  // Purchase
  // ===========================
  async addPurchaseEvent(email: string, currency: currency, value: number, contents: Contents[]) {
    const eventId = uuidv4();
    const hashedEmail = await this.hashSHA256(email);
    this.track('Purchase', { currency, value, contents, ...(email ? {user_data: {em:[hashedEmail]}} : {}) }, eventId);
    const fbp = this.getCookie("fbp");
    const fbc = this.getCookie("fbc");
    const request:MetaPixelEventRequest = {
      data: [
        {
          event_name: 'Purchase',
          event_time: this.getEpochTime(),
          event_id: eventId,
          action_source: 'website',
          event_source_url: this.isBrowser ? window.location.href : '',
          user_data: {
            em:[hashedEmail],
            client_user_agent: navigator.userAgent,
            ...(fbp ? {fbp: fbp } : {}),
            ...(fbc ? {fbc: fbc } : {})
          },
          custom_data: {
            currency,
            value,
            contents
          }
        }
      ]
    };
    this.sendMetaPixelEvent(request);
  }

  sendMetaPixelEvent(request: MetaPixelEventRequest) {
    if(!this.isProd) return;
    this.http.post(this.sendMetaPixelEventUrl, request).subscribe();
  }

  private async hashSHA256(input: string): Promise<string> {
  if (!input) {
    throw new Error("Input cannot be null");
  }
  const normalized = input.trim().toLowerCase();
  const encoder = new TextEncoder();
  const data = encoder.encode(normalized);
  
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  
  return hashHex;
}
  private getEpochTime(): number {
    return Math.floor(Date.now() / 1000);
  }
  private getCookie(name: string): string | null {
  const match = document.cookie.match(
    new RegExp('(^| )' + name + '=([^;]+)')
  );
  return match ? match[2] : null;
}

}