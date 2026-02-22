import { Component, computed, effect, ElementRef, inject, ViewChild, signal, Signal } from '@angular/core';
import { ParamsService } from '../../../core/services/params.service';
interface BannerContent{
  fix: boolean,
  speed: number,
  texts: Texts[]
}
interface Texts{
  text: string
}
@Component({
  selector: 'app-banner',
  imports: [],
  templateUrl: './banner.component.html',
  styleUrl: './banner.component.css'
})
export class BannerComponent {
  private paramService = inject(ParamsService);
  currentYear = new Date().getFullYear();
  activeBannerSection = computed(() => this.paramService.allPageSections().find(section => section.type === 'BANNER_SCROLLING' && section.active));
  bannerContent:Signal<BannerContent | null> =computed(() => {
    const content = this.activeBannerSection()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        fix: parsed.fix ,
        speed: parsed.speed,
        texts: parsed.texts
      };
    }
    return null;
  })
   private bannerRef = signal<ElementRef<HTMLDivElement> | null>(null);
  @ViewChild('banner', { static: false }) 
  set bannerElement(el: ElementRef<HTMLDivElement>) {
    if (el) {
      this.bannerRef.set(el); // Update signal when ViewChild is set
    }
  }

/*************  ✨ Windsurf Command ⭐  *************/
/**
 * Returns the ElementRef of the banner section.
 * @returns {ElementRef<HTMLDivElement> | null}
 */
  get banner() {
    return this.bannerRef();
  }


  // Animation variables
  duration = '20s';
  startPos = '100%';
  endPos = '-100%';

  constructor() {
    effect(() => {
      // React to changes in both banner ref and active items
      const banner = this.bannerRef();
      const items = this.activeBannerSection();
      
      if (banner && items) {
        setTimeout(() => this.calculateAnimation(), 0);
      }
    });
  }

  ngOnInit(): void {
    this.loadBannerSection();
  }

  loadBannerSection() {
    this.paramService.getSectionsByType('BANNER_SCROLLING').subscribe();
  }

  private calculateAnimation() {
    const banner = this.bannerRef();
    if (!banner) {
      return;
    }

    const el = banner.nativeElement;
    const textWidth = el.scrollWidth;          // width of text
    const screenWidth = window.innerWidth;     // width of screen

    // speed (100px per second)
    const speed = this.bannerContent()?.speed;
    this.duration = (textWidth / speed!) + 's';
    

    // Start fully off-screen on the right
    this.startPos = `${screenWidth}px`;

    // End fully off-screen on the left
    this.endPos = `-${textWidth}px`;
  }
}
