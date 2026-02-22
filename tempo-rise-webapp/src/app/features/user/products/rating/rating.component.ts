import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rating',
  imports: [CommonModule],
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.css']
})
export class RatingComponent {
  @Input() rating: number = 0;
  @Input() reviewCount: number|undefined = 0;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';

  get starSize() {
    return {
      'sm': 'w-3 h-3',
      'md': 'w-4 h-4',
      'lg': 'w-5 h-5'
    }[this.size];
  }

  get textSize() {
    return {
      'sm': 'text-xs',
      'md': 'text-sm',
      'lg': 'text-base'
    }[this.size];
  }

  getStarWidth(starPosition: number): number {
    if (this.rating >= starPosition) return 100;
    if (this.rating > starPosition - 1) {
      return Math.round((this.rating - (starPosition - 1)) * 100);
    }
    return 0;
  }
}