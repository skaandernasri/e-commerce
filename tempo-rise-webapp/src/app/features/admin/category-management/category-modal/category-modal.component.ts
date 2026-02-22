// category-modal.component.ts
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Category } from '../../../../core/models/category';
import { CategoryService } from '../../../../core/services/category.service';
import { animate, style, transition, trigger } from '@angular/animations';

@Component({
  selector: 'app-category-modal',
  standalone: true,
  imports: [CommonModule, FormsModule,ReactiveFormsModule],
  templateUrl: './category-modal.component.html',
  animations: [
    trigger('fadeInUp', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate('300ms cubic-bezier(0.4, 0, 0.2, 1)', 
          style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('200ms cubic-bezier(0.4, 0, 0.2, 1)', 
          style({ opacity: 0, transform: 'translateY(20px)' }))
      ])
  ])
  ]
})
export class CategoryModalComponent {
  categoryForm: FormGroup;
  category: Partial<Category> = { nom: '', description: '' };
  isEditMode = false;
  isLoading = false;

  constructor(
    public dialogRef: MatDialogRef<CategoryModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { category: Category },
    private categoryService: CategoryService,
    private fb: FormBuilder

  ) {
    this.categoryForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: ['']
    });
    if (data.category) {
      this.category = { ...data.category };
      this.categoryForm.patchValue(this.category);
      this.isEditMode = true;
    }
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    const categoryData = {
      ...this.categoryForm.value,
      id: this.isEditMode ? this.data.category.id : null
    };
    const operation = this.isEditMode 
      ? this.categoryService.updateCategory(this.category.id!,categoryData)
      : this.categoryService.createCategory(categoryData);

    operation.subscribe({
      next: () => {
        this.isLoading = false;
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error:', err);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}