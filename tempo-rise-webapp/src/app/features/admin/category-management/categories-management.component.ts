import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Category } from '../../../core/models/category';
import { CategoryService } from '../../../core/services/category.service';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { CategoryModalComponent } from './category-modal/category-modal.component';
import { MatDialogModule } from '@angular/material/dialog';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
@Component({
  selector: 'app-categories-list',
  standalone: true,
  imports: [CommonModule, FormsModule,MatDialogModule,
    PaginationComponent
  ],
  templateUrl: './categories-management.component.html',
  styleUrls: [ './categories-management.component.css']
})
export class ViewCategoriesComponent implements OnInit {
  categories: Category[] = [];
  filteredCategories: Category[] = [];
  searchTerm = '';
  isLoading = true;
  errorMessage = '';
  selectedCategory: string = '';
  currentPage: number = 1;
  itemsPerPage: number = 10;

  constructor(
    private categoryService: CategoryService,
    private dialog: MatDialog
  ) {}


  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.filteredCategories = [...categories];
        this.isLoading = false;
      },
      error: (err) => {
        if(err.status=404){
          this.categories=[]
          this.filteredCategories=[]
        }
        //this.errorMessage = 'Failed to load categories';
        this.isLoading = false;
      }
    });
  }

  filterCategories() {
    this.filteredCategories = this.categories.filter(cat =>
      (this.searchTerm ? cat.nom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
       cat.description.toLowerCase().includes(this.searchTerm.toLowerCase()) : true) &&
      (this.selectedCategory ? cat.nom === this.selectedCategory : true)
    );
    this.currentPage=1;
    this.paginatedCategories;

  }

  // editCategory(categoryId: number): void {
  //   this.router.navigate([`/admin/categories/edit/${categoryId}`]);
  // }

  deleteCategory(categoryId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Category',
        message: 'Are you sure you want to delete this category?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.categoryService.deleteCategory(categoryId).subscribe({
          next: () => {
            this.loadCategories();
          },
          error: (err) => {
            this.errorMessage = 'Failed to delete category';
          }
        });
      }
    });
  }
  // Open modal to add category
  openCreateCategory(): void {
    const dialogRef = this.dialog.open(CategoryModalComponent, {
      data: { category: null },
     // panelClass: ['rounded-lg', 'shadow-xl'] ,
      //backdropClass: 'transparent-backdrop',
     // disableClose: true
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadCategories();
    });
  }
  
  editCategory(categoryId: number): void {
    const category = this.filteredCategories.find(c => c.id === categoryId);
    if (category) {
      const dialogRef = this.dialog.open(CategoryModalComponent, {
        data: { category: category },
        panelClass: ['rounded-lg', 'shadow-xl'] // Add Tailwind-like classes
      });
  
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.loadCategories();
        }
      });
    }
  }
  onPageChange(newPage: number): void {
    this.currentPage = newPage;
  }
get paginatedCategories() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredCategories.slice(startIndex, startIndex + this.itemsPerPage);
  }
}