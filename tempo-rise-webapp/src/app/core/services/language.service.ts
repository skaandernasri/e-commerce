import { effect, Injectable, Renderer2, RendererFactory2, signal, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private readonly LANGUAGE_KEY = 'selectedLanguage';
  private translate = inject(TranslateService);
  private rendererFactory = inject(RendererFactory2);
  private renderer: Renderer2;
  
  public currentLanguage: string = 'fr';

  constructor() {
    // Create renderer from factory
    this.renderer = this.rendererFactory.createRenderer(null, null);
    effect(() => this.switchLangHtml());
  }

  private _languageSignal = signal<string>(
    typeof window !== 'undefined' && localStorage.getItem(this.LANGUAGE_KEY) || 'fr'
  );
  
  public language = this._languageSignal.asReadonly();

  initializeLanguage() {
    const savedLanguage = typeof window !== 'undefined' 
      ? localStorage.getItem(this.LANGUAGE_KEY) || 'fr' 
      : 'fr';
    
    this.translate.setDefaultLang('fr');
    this.translate.use(savedLanguage);
    this.currentLanguage = savedLanguage;
    this._languageSignal.set(savedLanguage);
  }

  changeLanguage(language: string) {
    if (typeof window !== 'undefined') {
      localStorage.setItem(this.LANGUAGE_KEY, language);
    }
    this.translate.use(language);
    this.currentLanguage = language;
    this._languageSignal.set(language);
  }

  switchLangHtml() {
    const lang = this._languageSignal();
    const html = document.documentElement;

    if (lang === 'ar') {
      this.renderer.setAttribute(html, 'dir', 'rtl');
      this.renderer.setAttribute(html, 'lang', lang);
    } else {
      this.renderer.setAttribute(html, 'dir', 'ltr');
      this.renderer.setAttribute(html, 'lang', lang);
    }
  }
}