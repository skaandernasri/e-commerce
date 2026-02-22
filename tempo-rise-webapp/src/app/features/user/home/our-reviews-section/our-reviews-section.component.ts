import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { Avis } from '../../../../core/models/avis';
import { AvisService } from '../../../../core/services/avis.service';
import { ParamsService } from '../../../../core/services/params.service';
import { environment } from '../../../../../environments/environment';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { ExpandLongTextComponent } from '../../../../shared/components/expand-long-text/expand-long-text.component';
import { ParamSectionResponse } from '../../../../core/models/params';
interface OurReviews{
  title: string;
}
@Component({
  selector: 'app-our-reviews',
  imports: [TranslateModule,CommonModule,ExpandLongTextComponent],
  templateUrl: './our-reviews-section.component.html',
  styleUrl: './our-reviews-section.component.css'
})
export class OurReviewsSectionComponent implements OnInit {
@Input() activeOurReviewsSection!: Signal<ParamSectionResponse | undefined>
private avisService = inject(AvisService);
imageBaseUrl = environment.imageUrlBase;
 ourReviewsContent:Signal<OurReviews | null> =computed(() => {
    const content = this.activeOurReviewsSection()!.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title
      };
    }
    return null;
  })
  reviews :Avis[]=[];
  reviewLoading = this.avisService.isLoading$;
  selectedReviewIndex: number = 1; // default center one
  ngOnInit(): void {
      this.loadReviews(); 
  }
  loadReviews(){
    this.avisService.getAllAvis().subscribe({
      next: (reviews) => {
       this.reviews = reviews
      },
      error: (error) => {
        console.error(error);
      }
    });
  }
  visibleStartIndex = 0;
  get visibleReviews() {
  const total = this.reviews.length;
  const getIndex = (i: number) => (i + total) % total;
  
  if (total === 0) return [];
  if (total === 1) return this.reviews;
  
  if (total === 2) {
    // Rotate the array based on selectedReviewIndex
    return this.selectedReviewIndex === 0 
      ? [this.reviews[0], this.reviews[1]]
      : [this.reviews[1], this.reviews[0]];
  }
  
  // For 3+ reviews
  return [
    this.reviews[getIndex(this.selectedReviewIndex - 1)], // left review
    this.reviews[getIndex(this.selectedReviewIndex)],     // center review
    this.reviews[getIndex(this.selectedReviewIndex + 1)]  // right review
  ];
}

getCenterIndex(): number {
  // For 2 items, the centered one is always at index 0
  // For other counts, it's the middle
  return this.visibleReviews.length === 2 ? 0 : Math.floor(this.visibleReviews.length / 2);
}

moveLeft() {
  if (this.reviews.length <= 1) return;
  this.selectedReviewIndex = (this.selectedReviewIndex - 1 + this.reviews.length) % this.reviews.length;
}

moveRight() {
  if (this.reviews.length <= 1) return;
  this.selectedReviewIndex = (this.selectedReviewIndex + 1) % this.reviews.length;
}
}
