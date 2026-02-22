import { Component, OnInit, OnDestroy, inject, effect, computed, signal, untracked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ProductService } from '../../../core/services/product.service';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { ProductModalComponent } from './product-modal/product-modal.component';
import { MatDialogModule } from '@angular/material/dialog';
import { CategoryService } from '../../../core/services/category.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { Subject, takeUntil } from 'rxjs';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { SwalService } from '../../../core/services/swal-service.service';
import { SpinnerComponent } from "../../../shared/components/spinner/spinner.component";
import { ExpandLongTextComponent } from "../../../shared/components/expand-long-text/expand-long-text.component";
import { QuillModule } from "ngx-quill";
import { I } from '@angular/cdk/a11y-module.d-DBHGyKoh';


@Component({
  selector: 'app-view-products',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, PaginationComponent,
    CurrencyFormatPipe, SpinnerComponent, ExpandLongTextComponent, QuillModule],
  templateUrl: './products-management.component.html',
  styleUrls: ['./products-management.component.css']
})
export class ViewProductsComponent implements  OnDestroy , OnInit{
  private productService=inject(ProductService)
  private categoryService=inject(CategoryService)
  products = this.productService.adminProducts
  categories=this.categoryService.categories$
  searchTerm = signal('');
  isLoadingProducts = this.productService.isLoading;
  isLoadingCategories = this.categoryService.isLoading$;
  selectedCategory=signal('');
  totalElements = this.productService.totalElements;
  totalPages = this.productService.totalPages;
  pagination = this.productService.pagination;
  isLoading = false;
  toggleIsLoading = false;
  
  private destroy$ = new Subject<void>();
private previousCategory = this.selectedCategory();
private previousSearch = this.searchTerm();
  constructor(
    private dialog: MatDialog,
    private swalService: SwalService
  ) {


effect(() => {
  const category = this.selectedCategory();
  const search = this.searchTerm();

  untracked(() => {
    this.resetCurrentPage();
  });
});
effect(() =>{
  const page = this.pagination();
  untracked(() => {
    this.loadProducts();
  })
})


  }
  ngOnInit(): void {
      this.loadCategories();
      this.loadProducts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadProducts(): void {
  const params: any = {
    page: this.pagination().page,
    size: this.pagination().size
  };
  if (this.selectedCategory()) params.categoryNames = this.selectedCategory();
  if (this.searchTerm()) params.productName = this.searchTerm();
  
    //this.isLoading = true;
    this.productService.loadAdminProducts(params);
    
  }

  loadCategories(): void {
    this.categoryService.getAllCategoriesSignal();
  }
  
  openCreateProduct(): void {
    const dialogRef = this.dialog.open(ProductModalComponent, {
      data: { product: null }
    });

    dialogRef.afterClosed().pipe(
      takeUntil(this.destroy$)).subscribe((isUpdated) => {
        if(isUpdated)
          this.loadProducts();
      });
  }

  editProduct(productId: number): void {
    const product = this.products().find(p => p.id === productId);
    if (product) {
      const dialogRef = this.dialog.open(ProductModalComponent, {
        data: { product: product }
      });

      dialogRef.afterClosed().pipe(
        takeUntil(this.destroy$)).subscribe({
          next: (isUpdated) => {
            if(isUpdated){
            this.loadProducts();
            this.swalService.success('SWAL.PRODUCT.SUCCESS_UPDATE');
            }
          },
          error: () => {
            this.swalService.error('SWAL.PRODUCT.FAILED_UPDATE');
          }
        })
    }
  }

  deleteProduct(productId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Product',
        message: 'Are you sure you want to delete this product?'
      }
    });

    dialogRef.afterClosed().pipe(
      takeUntil(this.destroy$))
      .subscribe(result => {
        if (result) {
          this.productService.deleteProduct(productId).pipe(
            takeUntil(this.destroy$)).subscribe({
              next: () => {
                this.loadProducts();
                this.swalService.success('SWAL.PRODUCT.SUCCESS_DELETE');
              },
              error: () => {
                this.swalService.error('SWAL.PRODUCT.FAILED_DELETE');
              }
            });
      }
    });
  }
toggleProductStatus(id: number): void {
  const product = this.products().find(p => p.id === id);
  if (!product) {
    return};
  const updatedProduct={
    ...product,
    actif: !product.actif
  }
  this.productService.updateProduct(product.id, updatedProduct).subscribe({
    next: () => {
      this.loadProducts();
      this.swalService.success('SWAL.PRODUCT.SUCCESS_UPDATE');
    },
    error: (err) => {
      console.error('Error updating status', err);
      this.swalService.error('SWAL.PRODUCT.FAILED_UPDATE');
    }
  });
}


  resetCurrentPage(): void {
    this.productService.resetCurrentPage();
    this.loadProducts();
  }
  onPageChange(newPage: number): void {
    this.productService.updateCurrentPage(newPage);
  }
}