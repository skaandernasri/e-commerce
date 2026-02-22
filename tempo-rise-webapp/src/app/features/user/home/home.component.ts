import { AfterViewInit, Component, computed, effect, inject, OnInit, signal, Signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../../core/services/product.service';
import { AvisService } from '../../../core/services/avis.service';
import { Avis } from '../../../core/models/avis';
import { LanguageService } from '../../../core/services/language.service';
import { ExpandLongTextComponent } from '../../../shared/components/expand-long-text/expand-long-text.component';
import { ProductCardComponent } from "../products/product-card/product-card.component";
import { LogoSpinnerComponent } from "../../../shared/components/logo-spinner/logo-spinner.component";
import { ParamsService } from '../../../core/services/params.service';
import { environment } from '../../../../environments/environment';
import { animate, style, transition, trigger } from '@angular/animations';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import { HeroSectionComponent } from './hero-section/hero-section.component';
import { FeaturesSectionComponent } from './features-section/features-section.component';
import { AdvantagesSectionComponent } from './advantages-section/advantages-section.component';
import { HowItWorksSectionComponent } from './how-it-works-section/how-it-works-section.component';
import { PositivitySectionComponent } from './positivity-section/positivity-section.component';
import { MissionSectionComponent } from './mission-section/mission-section.component';
import { FinalCtaSectionComponent } from './final-cta-section/final-cta-section.component';
import { LearnMoreSectionComponent } from './learn-more-section/learn-more-section.component';
import { FaqSectionComponent } from './faq-section/faq-section.component';
import { NewLetterSectionComponent } from './new-letter-section/new-letter-section.component';
import { OurProductsSectionComponent } from './our-products-section/our-products-section.component';
import { OurReviewsSectionComponent } from './our-reviews-section/our-reviews-section.component';
interface HeroContent {
  title: string;
  subtitle: string;
  buttonText: string;
  buttonLink: string;
}
interface OurProductsContent{
  title: string;
  buttonText: string;
  buttonLink: string;
}
interface OurReviews{
  title: string;
}
interface content1Content{
  title: string;
  subtitle: string;
  imagesAndText: ImageTextPair[];
}
interface ImageTextPair {
  index: number;
  imageUrl: string;
  title: string;
  subtitle: string;
}
interface content5Content{
  title: string;
  subtitle: string;
  title1: string;
  subtitle1: string;
  title2: string;
  subtitle2: string;
  title3: string;
  subtitle3: string;
}
interface FaqContent {
  title: string;
  subtitle: string;
  qna: FaqItem[];
}
interface FaqItem {
  question: string;
  answer: string;
  active: boolean;
}
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    LogoSpinnerComponent,HeroSectionComponent,FeaturesSectionComponent,AdvantagesSectionComponent, 
  HowItWorksSectionComponent,PositivitySectionComponent,MissionSectionComponent,
FinalCtaSectionComponent, LearnMoreSectionComponent, FaqSectionComponent,
NewLetterSectionComponent, OurProductsSectionComponent, OurReviewsSectionComponent], // Import TranslateModule
  templateUrl: './home.component.html',
  animations: [
    trigger('transition', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('500ms ease-in-out', style({ opacity: 1 }))
      ])
      
    ])]})
export class HomeComponent implements OnInit{
    private productService = inject(ProductService);
    private paramService = inject(ParamsService);
    private avisService = inject(AvisService);
   
 homePageIsLoading = computed(() => this.productService.isLoading() && this.avisService.isLoading$()  && this.paramService.landingPageSectionsIsLoading())
 activeHeroSection = computed(() => this.paramService.landingPageSections().find(section => section.type === 'HERO' && section.active));
 activeContent1Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT1' && section.active));
 activeContent2Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT2' && section.active));
 activeContent3Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT3' && section.active));
 activeContent4Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT4' && section.active));
 activeContent5Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT5' && section.active));
 activeContent6Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT6' && section.active));
 activeContent7Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT7' && section.active));
 activeContent8Section = computed(() => this.paramService.landingPageSections().find(section => section.type === 'CONTENT8' && section.active));
 activeFaqSection = computed(() => this.paramService.landingPageSections().find(section => section.type === 'FAQ' && section.active));
 activeOurProductsSection = computed(() => this.paramService.landingPageSections().find(section => section.type === 'OUR_PRODUCTS' && section.active));
 activeOurReviewsSection = computed(() => this.paramService.landingPageSections().find(section => section.type === 'OUR_REVIEWS' && section.active));
 faqs: { question: string; answer: string }[] = [];
  constructor() {}
  ngOnInit(): void {
      this.paramService.getSectionsByPageType('HOME').subscribe();
  }
fullPageScroll() {
    const sections = Array.from(document.querySelectorAll('section'));
    let index = 0;
    let locked = false;

    // custom smooth scroll
    const smoothScrollTo = (target: number, duration = 400) => {
      const start = window.scrollY;
      const change = target - start;
      const startTime = performance.now();

      const animate = (time: number) => {
        const progress = Math.min((time - startTime) / duration, 1);

        // smooth easing (easeInOutQuad)
        const ease = progress < 0.5
          ? 2 * progress * progress
          : 1 - Math.pow(-2 * progress + 2, 2) / 2;

        window.scrollTo(0, start + ease * change);

        if (progress < 1) requestAnimationFrame(animate);
      };

      requestAnimationFrame(animate);
    };

    window.addEventListener('wheel', (e) => {
      if (locked) return; // block multiple scrolls
      locked = true;

      if (e.deltaY > 0 && index < sections.length - 1) {
        index++; // scroll down
      } else if (e.deltaY < 0 && index > 0) {
        index--; // scroll up
      }

      const targetPosition = sections[index].offsetTop;
      smoothScrollTo(targetPosition);

      setTimeout(() => locked = false, 800); // unlock after animation
    });
  }

  }
  
