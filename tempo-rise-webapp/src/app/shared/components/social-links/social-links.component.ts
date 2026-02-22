import { Component, computed, inject, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../core/services/params.service';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';
interface RightBannerSection {
  imageUrl: string;
  text: string;
  url: string;
}
@Component({
  selector: 'app-social-links',
  // animate slider
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(100%)' }),
        animate('200ms ease-in', style({ transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(100%)' }))
      ])
    ]),
    trigger('slideInOutIcon', [
      transition(':enter', [
        style({ transform: 'translateX(0)' }),
        animate('200ms ease-in', style({ transform: 'translateX(-100%)' }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(0)' }))
      ])
    ]),
  ],
  imports: [CommonModule],
  templateUrl: './social-links.component.html',
  styleUrl: './social-links.component.css'
})
export class SocialLinksComponent implements OnInit {
  private paramService = inject(ParamsService);
  currentYear = new Date().getFullYear();
  imageBaseUrl = environment.imageUrlBase
  activeRightBarSections = computed(() => this.paramService.allPageSections().find(section => section.type === 'RIGHT_BARS' && section.active));
  hoveredBar=-1;
  rightBarContent:Signal<RightBannerSection[] | null> =computed(() => {
    const content = this.activeRightBarSections()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return parsed.bar.map((item: any) => ({ imageUrl: item.imageUrl, text: item.text, url: item.url }));
    }
    return null;
  })
    ngOnInit() {
    this.loadRightBarSections();
  }
    loadRightBarSections() {
    this.paramService.getSectionsByType('RIGHT_BARS').subscribe();
  }
  mouseOverIndex(event: Event, index: number) {
    this.hoveredBar = index;
  }

}
