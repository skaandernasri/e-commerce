import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-pagination',
  imports: [TranslateModule],
template: `
  <div class="mt-6 flex justify-between items-center">
    <p class="text-sm text-[var(--text)]">
      {{ 'PAGINATION.SHOWING' | translate:{ start: startIndex, end: endIndex, total: totalItems } }}
    </p>

    <div class="flex gap-2">
      <button 
        (click)="changePage(currentPage - 1)"
        [disabled]="currentPage === 1"
        class="px-3 py-1 border rounded hover:bg-[var(--secondary)] disabled:opacity-50"
      >
        {{ 'PAGINATION.PREVIOUS' | translate }}
      </button>
      
      @for (page of pages; track page) {
        <button 
          (click)="changePage(page)"
          [class.bg-[var(--primary)]]="currentPage === page"
          [class.text-white]="currentPage === page"
          class="px-3 py-1 border rounded hover:bg-[var(--secondary)]"
        >
          {{ page }}
        </button>
      }
      
      <button 
        (click)="changePage(currentPage + 1)"
        [disabled]="currentPage === totalPages || totalItems <= itemsPerPage"
        class="px-3 py-1 border rounded hover:bg-[var(--secondary)] disabled:opacity-50"
      >
        {{ 'PAGINATION.NEXT' | translate }}
      </button>
    </div>
  </div>
`,
    })
export class PaginationComponent {
  @Input() currentPage: number = 1;
  @Input() itemsPerPage: number = 10;
  @Input() totalItems: number = 0;
  @Output() pageChange = new EventEmitter<number>();

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.itemsPerPage);
  }

  get startIndex(): number {
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  get endIndex(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.totalItems);
  }

  get pages(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      for (let i = 1; i <= this.totalPages; i++) pages.push(i);
    } else {
      const half = Math.floor(maxVisiblePages / 2);
      let start = Math.max(1, this.currentPage - half);
      const end = Math.min(this.totalPages, start + maxVisiblePages - 1);
      
      if (end - start + 1 < maxVisiblePages) {
        start = end - maxVisiblePages + 1;
      }
      
      for (let i = start; i <= end; i++) pages.push(i);
    }
    
    return pages;
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.pageChange.emit(page);
    }
  }
}
