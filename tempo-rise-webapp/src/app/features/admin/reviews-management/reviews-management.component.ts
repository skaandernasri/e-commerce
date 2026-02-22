import {
  Component,
  OnInit,
  signal,
  effect,
  untracked,
  WritableSignal
} from '@angular/core';
import { AvisService } from '../../../core/services/avis.service';
import { Avis } from '../../../core/models/avis';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product';
import { RatingComponent } from '../../user/products/rating/rating.component';
import { ExpandLongTextComponent } from '../../../shared/components/expand-long-text/expand-long-text.component';

@Component({
  selector: 'app-reviews-management',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent, RatingComponent, ExpandLongTextComponent],
  templateUrl: './reviews-management.component.html',
  styleUrl: './reviews-management.component.css'
})
export class ReviewsManagementComponent implements OnInit {
  products: Product[] = [];
  reviews = signal<Avis[]>([]);

  searchTerm = signal('');
  selectedProduct = signal('');
  sortDirection = signal<'asc' | 'desc' | ''>('');
  currentPage = signal(1);
  itemsPerPage = 10;
  totalPages = signal(1);
  totalElements = signal(0);
  isLoading = true;
  errorMessage = '';
  expandedReviews = new Set<string>();
  searchMode: WritableSignal<'email' | 'comment' | 'both'> = signal('both'); // 'email' | 'comment' | 'both';
  averageFromDb = 0;


  // Metrics
  currentWeekReviews = 0;
  lastWeekReviews = 0;
  currentWeekRating = 0;
  lastWeekRating = 0;
  reviewChangePercentage = 0;
  ratingChangePercentage = 0;

  constructor(
    private reviewService: AvisService,
    private dialog: MatDialog,
    private productService: ProductService,
  ) {
    effect(() => {
      const searchTerm = this.searchTerm();
      const searchMode = this.searchMode();
      const selectedProduct = this.selectedProduct();
      untracked(() => this.resetCurrentPage());
    });
    effect(() => {
      this.currentPage();
      untracked(() => this.loadReviews());  
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadReviews();
  }


 

  private calculatePercentageChange(current: number, previous: number): number {
    if (previous === 0) return current > 0 ? 100 : 0;
    return ((current - previous) / previous) * 100;
  }

  hasIncreased(current: number, last: number): boolean {
    return current > last;
  }

  get averageRating(): number {
    const reviews = this.reviews();
    if (!reviews.length) return 0;
    return parseFloat((reviews.reduce((sum, r) => sum + r.note, 0) / reviews.length).toFixed(2));
  }

  sortByRating(): void {
    const current = this.sortDirection();
    this.sortDirection.set(current === 'asc' ? 'desc' : current === 'desc' ? 'asc' : 'desc');
    this.loadReviews(); // Reload with new sort direction
  }

  onPageChange(newPage: number): void {
    this.currentPage.set(newPage);
  }

  loadReviews(): void {
  const params: any = {
    page: this.currentPage(),
    size: this.itemsPerPage
  };

  if (this.selectedProduct()) params.productName = this.selectedProduct();
  
  if (this.searchTerm()) {
    switch (this.searchMode()) {
      case 'email':
        params.userEmail = this.searchTerm();
        break;
      case 'comment':
        params.comment = this.searchTerm();
        break;
      default: // 'both'
        params.userEmail = this.searchTerm();
        params.comment = this.searchTerm();
    }
  }
  
    this.isLoading = true;
    this.reviewService.getPagedAvis(
      params
    ).subscribe({
      next: (response) => {
        this.reviews.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.averageFromDb = response.averageRating;
        this.lastWeekRating = response.averageRatingsWeekAgo;
        this.currentWeekRating = response.averageRatingsThisWeek;
        this.lastWeekReviews = response.reviewsWeekAgo;
        this.currentWeekReviews = response.reviewsThisWeek;
        this.reviewChangePercentage = this.calculatePercentageChange(
        response.reviewsThisWeek,
        response.reviewsWeekAgo,
    );
    this.ratingChangePercentage= (response.averageRatingsThisWeek - response.averageRatingsWeekAgo) * 20;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.errorMessage = 'Failed to load reviews';
      }
    });
  }
  
  loadProducts(): void {
    this.productService.getAllProducts().subscribe({
      next: products => {
        this.products = products;
      },
      error: err => {
        console.error(err);
      }
    });
  }

  resetCurrentPage(): void {
    this.currentPage.set(1);
    this.loadReviews();
  }

  deleteReview(reviewId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Review',
        message: 'Are you sure you want to delete this review?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.reviewService.deleteAvis(reviewId).subscribe({
          next: () => this.loadReviews(),
          error: () => (this.errorMessage = 'Failed to delete review')
        });
      }
    });
  }
}