import {
  Component,
  OnInit,
  Inject,
  ViewChild,
  ElementRef,
  OnDestroy
} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  FormBuilder,
  FormGroup,
  Validators,
  FormArray,
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { animate, style, transition, trigger } from '@angular/animations';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { takeUntil, switchMap, catchError, finalize } from 'rxjs/operators';

import { Product, variantRequest, variantResponse } from '../../../../core/models/product';
import { Category } from '../../../../core/models/category';
import { ImageProduct } from '../../../../core/models/image-product';
import { Promotion } from '../../../../core/models/promotion';

import { ProductService } from '../../../../core/services/product.service';
import { ImageProductService } from '../../../../core/services/image-product.service';
import { CategoryService } from '../../../../core/services/category.service';
import { PromotionService } from '../../../../core/services/promotion.service';
import { VariantService } from '../../../../core/services/variant.service';
import { QuillModule } from 'ngx-quill';
import { CommonModule } from '@angular/common';
import { QUILL_MODULES } from '../../../../shared/configs/quill-config';
import { environment } from '../../../../../environments/environment';
import { SwalService } from '../../../../core/services/swal-service.service';

@Component({
  selector: 'app-product-modal',
  templateUrl: './product-modal.component.html',
  imports: [FormsModule, ReactiveFormsModule, QuillModule, CommonModule],
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
export class ProductModalComponent implements OnInit, OnDestroy {
  // --- Data Collections ---
  categories: Category[] = [];
  promotions: Promotion[] = [];
  availablePromotions: Promotion[] = [];

  // --- Product & Form ---
  product: Partial<Product> = {
    nom: '',
    description: '',
    prix: 0,
    categorie: {} as Category,
    imageProduits: [],
    promotions: []
  };
  productForm: FormGroup;

  // --- UI States ---
  imagesBaseUrl = environment.imageUrlBase;
  imagePreviews: string[] = [];
  selectedFiles: File[] = [];
  selectedPromotion: Promotion | null = null;
  promotionsPreview: Promotion[] = [];
  isEditMode = false;
  isLoading = false;
  isDeletingImage = false;

  // Track variants to delete on submit
  private variantsToDelete: number[] = [];
  private destroy$ = new Subject<void>();

  @ViewChild('productModal') productModal!: ElementRef;

  // --- Options ---
  colorOptions = [
    'White', 'Black', 'Gray', 'Red', 'Pink', 'Orange', 'Yellow',
    'Green', 'Blue', 'Purple', 'Brown', 'Beige', 'Cyan', 'Magenta',
    'Maroon', 'Navy', 'Olive', 'Teal', 'Turquoise', 'Lavender',
    'Gold', 'Silver'
  ];

  sizeOptions = [
    '34', '36', '38', '40', '42', '44', '46', '48', '50',
    '52', '54', '56', '58'
  ];

  marques = [
    { value: 'adidas', viewValue: 'Adidas' },
    { value: 'nike', viewValue: 'Nike' },
    { value: 'puma', viewValue: 'Puma' },
    { value: 'reebok', viewValue: 'Reebok' },
    { value: 'converse', viewValue: 'Converse' },
    { value: 'other', viewValue: 'Autre' }
  ];

  modules = QUILL_MODULES;

  constructor(
    public dialogRef: MatDialogRef<ProductModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { product: Product },
    private fb: FormBuilder,
    private productService: ProductService,
    private imageService: ImageProductService,
    private categorieService: CategoryService,
    private promotionService: PromotionService,
    private variantService: VariantService,
    private swalService: SwalService
  ) {
    this.productForm = this.createProductForm();
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // --- Initialization ---
  private createProductForm(): FormGroup {
    return this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(100)]],
      description: [''],
      prix: [0, [Validators.required, Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      quantite: [0, [Validators.required, Validators.min(0)]],
      categorie: [null, Validators.required],
      promotions: [[]],
      imageProduits: [[]],
      marque: ['', Validators.required],
      composition: [''],
      guide: [''],
      faq: [''],
      actif: [true],
      taille: [this.sizeOptions[0]],
      couleur: [this.colorOptions[0]],
      variants: this.fb.array([], [Validators.required])
    });
  }

  private loadInitialData(): void {
    forkJoin({
      categories: this.categorieService.getAllCategories(),
      promotions: this.promotionService.getAllPromotions().pipe(
        catchError(() => of([]))
      )
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ categories, promotions }) => {
          this.categories = categories;
          this.promotions = promotions;
          
          if (this.data.product) {
            this.initializeFormWithProduct(this.data.product);
            this.filterAvailablePromotions();
          }
        },
        error: (error) => console.error('Error loading initial data:', error)
      });
  }

  private filterAvailablePromotions(): void {
    this.availablePromotions = this.promotions.filter(p =>
      new Date(p.dateFin).getTime() > Date.now() &&
      p.produitId === this.product.id
    );
  }

  private initializeFormWithProduct(product: Product): void {
    this.isEditMode = true;
    this.promotionsPreview = product.promotions || [];
    this.product = { ...product };
    this.imagePreviews = product.imageProduits?.map(img => img.url) || [];

    // Initialize variants FormArray
    const variantsArray = this.variants;
    variantsArray.clear();
    product.variants?.sort((a, b) => +a.size - +b.size);
    product.variants?.forEach(v => variantsArray.push(this.createVariantGroup(v)));
    // Patch form values
    this.productForm.patchValue({
      nom: product.nom,
      description: product.description,
      prix: product.prix,
      quantite: 0,
      categorie: product.categorie,
      promotions: product.promotions,
      imageProduits: product.imageProduits || [],
      marque: product.marque || '',
      composition: product.composition,
      guide: product.guide,
      faq: product.faq,
      actif: product.actif
    });
  }

  // --- Variant Management ---
  get variants(): FormArray {
    return this.productForm.get('variants') as FormArray;
  }

  addVariant(): void {
    this.variants.push(this.createVariantGroup({ color: '', size: '', quantity: 1 }));
  }

  removeVariant(index: number): void {
    const variant = this.variants.at(index).value;
    
    // If variant has an ID, mark it for deletion on submit
    if (variant.id) {
      this.variantsToDelete.push(variant.id);
    }
    
    this.variants.removeAt(index);
  }

  // Track variants by their ID or a generated unique key
  trackVariant(index: number, control: any): any {
    const value = control.value;
    // Use ID if available, otherwise use a combination of properties
    return value.id || `${value.color}-${value.size}-${index}`;
  }

  private createVariantGroup(variant: Partial<variantResponse>): FormGroup {
    return this.fb.group({
      id: [variant.id || null],
      color: [variant.color || '', Validators.required],
      size: [variant.size || '', Validators.required],
      quantity: [variant.quantity || 0, [Validators.required, Validators.min(0)]]
    });
  }

  // --- Form Submission ---
  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.productForm.value;

    const productData = this.buildProductData(formValue);

    const productOperation$ = this.isEditMode
      ? this.productService.updateProduct(this.product.id!, productData)
      : this.productService.createProduct(productData);

    productOperation$
      .pipe(
        switchMap(savedProduct => this.handlePostProductSave(savedProduct, formValue)),
        finalize(() => this.isLoading = false),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (err) => console.error('Error saving product:', err)
      });
  }

  private buildProductData(formValue: any): any {
    return {
      ...formValue,
      promotions: formValue.promotions.map((p: Promotion) => ({ id: p.id })),
      imageProduits: formValue.imageProduits.map((url: any) => ({ url }))
    };
  }

  private handlePostProductSave(savedProduct: Product, formValue: any) {
    const operations: any[] = [];

    // Delete marked variants
    if (this.variantsToDelete.length > 0) {
      const deleteOperations = Array.from(this.variantsToDelete.map(id =>
        this.variantService.deleteVariant(id).pipe(catchError(() => of(null)))
      ));
      operations.push(...deleteOperations);
    }

    // Upload images
    if (this.selectedFiles.length > 0) {
      const imageUploads = this.selectedFiles.map(file =>
        this.imageService.uploadImage(savedProduct.id, file).pipe(
          catchError(err => {
            console.error('Error uploading image:', err);
            return of(null);
          })
        )
      );
      operations.push(...imageUploads);
    }

    // Update promotions
    if (formValue.promotions?.length > 0) {
      const promotionUpdates = formValue.promotions.map((promotion: Promotion) =>
        this.promotionService.updatePromotion(promotion.id, {
          ...promotion,
          produitId: savedProduct.id
        }).pipe(catchError(() => {return of(null)}))
      );
      
      operations.push(...promotionUpdates);
    }

    return operations.length > 0 
      ? forkJoin(operations as Observable<any>[]) 
      : of(null);
  }

  // --- Image Handling ---
  onFileEvent(event: Event | number): void {
    if (typeof event === 'number') {
      this.handleImageRemoval(event);
    } else {
      this.handleFileSelection(event as Event);
    }
  }

  private handleImageRemoval(index: number): void {
    const currentImages = [...this.productForm.value.imageProduits];
    const imageToRemove = currentImages[index];

    if (imageToRemove?.id) {
      this.isDeletingImage = true;
      this.imageService.deleteImage(imageToRemove.id)
        .pipe(
          finalize(() => this.isDeletingImage = false),
          takeUntil(this.destroy$)
        )
        .subscribe({
          next: () => this.removeImageFromArrays(index, currentImages),
          error: (err) => console.error('Error deleting image:', err)
        });
    } else {
      this.removeImageFromArrays(index, currentImages);
    }
  }

  private removeImageFromArrays(index: number, currentImages: any[]): void {
    currentImages.splice(index, 1);
    this.imagePreviews.splice(index, 1);
    this.selectedFiles.splice(index, 1);
    this.product?.imageProduits?.splice(index, 1);
    
    this.productForm.patchValue({ imageProduits: currentImages });
    this.productForm.get('imageProduits')?.markAsTouched();
  }

  private handleFileSelection(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const newFiles = Array.from(input.files);
    newFiles.forEach(file => {
      if(file.type !== 'image/jpeg' && file.type !== 'image/png' && file.type !== 'image/jpg') {
        this.swalService.error('SWAL.PRODUCTS.INVALID_IMAGE_TYPE');
        newFiles.splice(newFiles.indexOf(file), 1);
      }
    });
    this.selectedFiles = [...this.selectedFiles, ...newFiles];

    newFiles.forEach(file => {
      const reader = new FileReader();
      reader.onload = (e) => {
        if (e.target?.result) {
          this.imagePreviews.push(e.target.result as string);
        }
      };
      reader.readAsDataURL(file);
    });

    this.productForm.patchValue({
      imageProduits: [...this.productForm.value.imageProduits, ...newFiles]
    });

    input.value = '';
    this.productForm.get('imageProduits')?.markAsTouched();
  }

  // --- Promotion Management ---
  addPromotion(): void {
    if (!this.selectedPromotion) return;

    const currentPromotions = this.productForm.value.promotions || [];
    const exists = currentPromotions.some(
      (p: Promotion) => p.id === this.selectedPromotion!.id
    );

    if (!exists) {
      this.productForm.patchValue({
        promotions: [...currentPromotions, this.selectedPromotion]
      });
    }

    this.selectedPromotion = null;
  }

  removePromotion(index: number): void {
    const promotions = [...this.productForm.value.promotions];
    const promotionId = promotions[index].id;

    this.promotionService.deletePromotion(promotionId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          promotions.splice(index, 1);
          this.promotionsPreview.splice(index, 1);
          this.productForm.patchValue({ promotions });
        },
        error: (err) => console.error('Error deleting promotion:', err)
      });
  }

  // --- Utility ---
  compareCategories(c1: Category, c2: Category): boolean {
    return c1 && c2 ? c1.id === c2.id : c1 === c2;
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  handleClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInsideModal = this.productModal?.nativeElement.contains(target);
    
    if (!clickedInsideModal) {
      this.onCancel();
    }
  }
}