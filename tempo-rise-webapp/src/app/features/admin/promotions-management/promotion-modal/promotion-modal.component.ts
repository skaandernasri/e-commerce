import { animate, style, transition, trigger } from '@angular/animations';
import { ChangeDetectionStrategy, Component, Inject, OnInit } from '@angular/core';
import { Promotion } from '../../../../core/models/promotion';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PromotionService } from '../../../../core/services/promotion.service';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, ValidatorFn, AbstractControl, ValidationErrors, AsyncValidatorFn } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Product } from '../../../../core/models/product';
import { ProductService } from '../../../../core/services/product.service';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatTimepickerModule } from '@angular/material/timepicker';
import { SwalService } from '../../../../core/services/swal-service.service';
import { catchError, map, Observable, of } from 'rxjs';

@Component({
  selector: 'app-promotion-modal',
  standalone: true,
  providers: [provideNativeDateAdapter()],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatTimepickerModule
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './promotion-modal.component.html',
  styleUrl: './promotion-modal.component.css',
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
export class PromotionModalComponent implements OnInit {
  errMessage = '';
  promotionForm: FormGroup;
  isEditMode = false;
  isLoading = false;
  products: Product[] = [];

  constructor(
    public dialogRef: MatDialogRef<PromotionModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { promotion: Promotion },
    private promotionService: PromotionService,
    private productService: ProductService,
    private fb: FormBuilder,
    private swalService: SwalService
  ) {
    this.promotionForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      reduction: [1, [Validators.required, Validators.min(1)]],
      type:['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      produitId: [null, Validators.required],
      heureDebut: ['', Validators.required],
      heureFin: ['', Validators.required],
    }, {
  asyncValidators: [this.promotionTypeValidator()], // ✅ correct place
  validators: [this.promotionDateValidator(),this.promotionPercentageValidator()]
});
  }

  ngOnInit(): void {
    this.productService.getAllProducts().subscribe(products => {
      this.products = products;
      if (this.data.promotion) {
        this.isEditMode = true;
        const selectedProduct = this.products.find(product =>
          product.promotions?.find(promotion => promotion.id === this.data.promotion.id)
        );
        const { date: dateDebut, time: heureDebut } = this.formatDateTimeFromBackend(this.data.promotion.dateDebut);
        const { date: dateFin, time: heureFin } = this.formatDateTimeFromBackend(this.data.promotion.dateFin);

        this.promotionForm.patchValue({
          ...this.data.promotion,
          dateDebut,
          dateFin,
          heureDebut,
          heureFin,
          produitId: selectedProduct?.id
        });
      }
    });
  }

  private formatDateTimeFromBackend(utcDateString: string): { date: string, time: string } {
    if (!utcDateString) return { date: '', time: '' };
    const dateObj = new Date(utcDateString);
    const localYear = dateObj.getFullYear();
    const localMonth = String(dateObj.getMonth() + 1).padStart(2, '0');
    const localDay = String(dateObj.getDate()).padStart(2, '0');
    const localHours = String(dateObj.getHours()).padStart(2, '0');
    const localMinutes = String(dateObj.getMinutes()).padStart(2, '0');
    return {
      date: `${localYear}-${localMonth}-${localDay}`,
      time: `${localHours}:${localMinutes}`
    };
  }

  private convertLocalToUTC(dateString: string, timeString: string): string {
    const [hours, minutes] = timeString.split(':').map(Number);
    const localDate = new Date(dateString);
    localDate.setHours(hours, minutes, 0, 0);
    return localDate.toISOString();
  }

  onSubmit(): void {
    this.isLoading = true;
    const formValue = this.promotionForm.value;
    const utcDateDebut = this.convertLocalToUTC(formValue.dateDebut, typeof formValue.heureDebut === 'string' ? formValue.heureDebut : `${formValue.heureDebut.getHours().toString().padStart(2,'0')}:${formValue.heureDebut.getMinutes().toString().padStart(2,'0')}`);
    const utcDateFin = this.convertLocalToUTC(formValue.dateFin, typeof formValue.heureFin === 'string' ? formValue.heureFin : `${formValue.heureFin.getHours().toString().padStart(2,'0')}:${formValue.heureFin.getMinutes().toString().padStart(2,'0')}`);
    
    const promotionData = { ...formValue, dateDebut: utcDateDebut, dateFin: utcDateFin };
    const operation = this.isEditMode 
      ? this.promotionService.updatePromotion(this.data.promotion.id!, promotionData)
      : this.promotionService.createPromotion(promotionData);

    operation.subscribe({
      next: () => {
        this.isLoading = false;
        this.dialogRef.close(true);
        this.swalService.success('SWAL.PROMOTION.SAVED_SUCCESSFULLY');
      },
      error: (err) => {
        this.isLoading = false;
        this.errMessage = "Cannot have more than one active promotion per product";
        console.error('Error:', err);
        this.promotionForm.markAllAsTouched();
        this.swalService.error('SWAL.PROMOTION.FAILED_SAVE');
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  promotionDateValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const dateDebut = new Date(group.get('dateDebut')?.value);
      const dateFin = new Date(group.get('dateFin')?.value);
      const now = new Date();

      if (isNaN(dateDebut.getTime()) || isNaN(dateFin.getTime())) return null;
      if (dateDebut > dateFin) return { dateOrderInvalid: 'Start date must be before end date' };
      if (dateFin < now) return { endDateInPast: 'End date cannot be in the past' };
      return null;
    };
  }
promotionTypeValidator(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const type = control.get('type')?.value;
    const produitId = control.get('produitId')?.value;
    const reduction = control.get('reduction')?.value;

    if (type === 'FIXED' && produitId != null) {
      return this.productService.getProductById(produitId).pipe(
        map(product => {
          if (product.prix < reduction) {
            return { priceLessThanReduction: true };
          }
          return null;
        }),
        catchError(err => {
          console.error('Error:', err);
          return of(null);
        })
      );
    }

    return of(null);
  };
}
promotionPercentageValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const type = control.get('type')?.value;
      const reduction = control.get('reduction')?.value;
  
      if (type === 'PERCENTAGE' && (reduction <= 0 || reduction >= 100)) {
        return { percentageOutOfRange: true };
      }
  
      return null;
    };
  }
}
