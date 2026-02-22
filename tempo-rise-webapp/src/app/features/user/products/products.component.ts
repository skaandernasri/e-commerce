import { Component, computed, effect, inject, OnInit, OnDestroy, signal, ChangeDetectionStrategy, Signal, untracked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { ProductCardComponent } from './product-card/product-card.component';
import { MatSliderModule } from '@angular/material/slider';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { animate, style, transition, trigger } from '@angular/animations';
import { Subject, takeUntil, debounceTime } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { SpinnerComponent } from "../../../shared/components/spinner/spinner.component";
import { ParamsService } from '../../../core/services/params.service';

type FilterState = {
  price: boolean;
  category: boolean;
  promotion: boolean;
};

interface mainSection {
  title: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-products',
  standalone: true,
  templateUrl: './products.component.html',
  imports: [
    CommonModule,
    FormsModule,
    ProductCardComponent,
    MatSliderModule,
    PaginationComponent,
    TranslateModule,
    SpinnerComponent
  ],
  animations: [
    trigger('filterSlideIn', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ]),
    trigger('expandCollapse', [
      transition(':enter', [
        style({ height: '0', opacity: 0 }),
        animate('300ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ height: '0', opacity: 0 }))
      ])
    ])
  ]
})
export class ProductsComponent implements OnInit, OnDestroy {
  // Services
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private paramService = inject(ParamsService);

  activeProductsSection = computed(() => this.paramService.productsPageSections().filter(section => section.active));
  activeProductsOurProductsSection = computed(() => this.activeProductsSection().find(section => section.type === 'OUR_PRODUCTS'));
  
  mainContent: Signal<mainSection | null> = computed(() => {
    const content = this.activeProductsOurProductsSection()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title
      };
    }
    return null;
  });
  
  private destroy$ = new Subject<void>();

  // State
  products = this.productService.products;
  categories = this.categoryService.categories$;
  pagination = this.productService.pagination;
  totalElements = this.productService.totalElements;
  totalPages = this.productService.totalPages;
  maxPrice = this.productService.maxPrice;
  isLoadingProducts = this.productService.isLoading;

  // UI State
  isMobile = false;
  showFilters = true;
  allFiltersVisible = true;
  
  filters = signal({
    priceRange: [0, 5000] as [number, number],
    promotionRange: [0, 100] as [number, number],
    categories: {} as Record<number, boolean>,
  });

  filterStates = signal<FilterState>({
    price: false,
    category: false,
    promotion: false
  });

  constructor() {
    effect(() => {
      this.filters.update(f => ({
        ...f,
        priceRange: [0, this.maxPrice()]
      }))
      
    });
      effect(() =>{this.filters();
        untracked(() => {this.resetCurrentPage();});
      });
      effect(() =>{this.pagination();untracked(() => this.loadProducts());});
  }

  ngOnInit(): void {
    this.checkMobileView();
    this.loadInitialData();
    window.addEventListener('resize', this.handleResize);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    window.removeEventListener('resize', this.handleResize);
  }

  // Use arrow function to maintain 'this' context
  private handleResize = (): void => {
    this.checkMobileView();
  }

  // Data loading
  loadInitialData(): void {
    this.paramService.getSectionsByPageType('PRODUCT').subscribe();
    this.categoryService.getAllCategoriesSignal();
    this.loadProducts();
  }

  loadProducts(): void {
    const params = {
      page: this.pagination().page,
      size: this.pagination().size,
      minPrice: this.filters().priceRange[0],
      maxPrice: this.filters().priceRange[1],
      minPromotion: this.filters().promotionRange[0],
      maxPromotion: this.filters().promotionRange[1],
      categoryNames: this.getSelectedCategoryNames(),
      actif: true
    };
    
    this.productService.loadProductsListPage(params);

  }

  // Filter methods
  initializeCategoryFilters = computed(() => {
    if (this.categories().length) {
      this.filters.update(state => ({
      ...state,
      categories: this.categories().reduce((acc, cat) => ({ 
        ...acc, 
        [cat.id]: false 
      }), {})
    }));
    }
  });

  private getSelectedCategoryNames(): string[] {
    return this.categories()
      .filter(category => this.filters().categories[category.id])
      .map(category => category.nom);
  }

  // UI interaction methods
  onPriceChange(index: 0 | 1, event: Event): void {
    const value = +(event.target as HTMLInputElement).value;
    this.filters.update(f => ({
      ...f,
      priceRange: index === 0 ? [value, f.priceRange[1]] : [f.priceRange[0], value]
    }));

    //this.filterChanges.next();
  }

  onPromotionChange(index: 0 | 1, event: Event): void {
    const value = +(event.target as HTMLInputElement).value;
    this.filters.update(f => ({
      ...f,
      promotionRange: index === 0 ? [value, f.promotionRange[1]] : [f.promotionRange[0], value]
    }));
  }

  toggleCategory(categoryId: number): void {
    this.filters.update(f => ({
      ...f,
      categories: { ...f.categories, [categoryId]: !f.categories[categoryId] }
    }));
  }

  resetFilters(): void {
    this.filters.set({
      priceRange: [0, this.maxPrice()],
      promotionRange: [0, 100],
      categories: this.categories().reduce((acc, cat) => ({ ...acc, [cat.id]: false }), {})
    });
  }

  // UI helpers
  checkMobileView(): void {
    this.isMobile = window.innerWidth < 1024;
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  toggleAllFilters(): void {
    this.allFiltersVisible = !this.allFiltersVisible;
    this.filterStates.update(() => ({
      price: this.allFiltersVisible,
      category: this.allFiltersVisible,
      promotion: this.allFiltersVisible
    }));
  }

  toggleFilter(filterName: keyof FilterState): void {
    this.filterStates.update(state => ({
      ...state,
      [filterName]: !state[filterName]
    }));
  }

  // Pagination
    resetCurrentPage(): void {
    this.productService.resetCurrentPage();
    this.loadProducts();
  }
  onPageChange(page: number): void {
    this.productService.updateCurrentPage(page);  }

  // Formatting
  formatLabel(value: number): string {
    return value >= 1000 ? `${Math.round(value / 1000)}k` : `${value}`;
  }
  
}