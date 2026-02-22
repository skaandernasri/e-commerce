import { animate, style, transition, trigger } from '@angular/animations';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CodePromo } from '../../../../core/models/code-promo';
import { CodePromoService } from '../../../../core/services/code-promo.service';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTimepickerModule } from '@angular/material/timepicker';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';

@Component({
  selector: 'app-code-promo-modal',
  providers: [provideNativeDateAdapter()],
  imports: [CommonModule, ReactiveFormsModule, MatDatepickerModule,
    MatFormFieldModule, MatInputModule, MatTimepickerModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './code-promo-modal.component.html',
  styleUrl: './code-promo-modal.component.css',
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
export class CodePromoModalComponent {
  codeForm: FormGroup;
  isEditMode = false;
  isLoading = false;
  errMessage = '';
  
  constructor(
    public dialogRef: MatDialogRef<CodePromoModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { code: CodePromo },
    private codePromoService: CodePromoService,
    private fb: FormBuilder
  ) {
    this.codeForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(2)]],
      reduction: [1, [Validators.required, Validators.min(1), Validators.max(100)]],
      dateExpiration: ['', Validators.required],
      timeExpiration: ['', Validators.required]
    });
    
    this.dialogRef.backdropClick().subscribe(() => {
      this.onCancel();
    });
  }
  
  ngOnInit(): void {
    if (this.data.code) {
      this.isEditMode = true;
      const { date: dateExpiration, time: timeExpiration } = this.formatDateTimeFromBackend(this.data.code.dateExpiration);
      this.codeForm.patchValue({
        ...this.data.code,
        dateExpiration,
        timeExpiration
      });
    }
  }


private formatDateTimeFromBackend(utcDateString: string): { date: string, time: string } {
  if (!utcDateString) return { date: '', time: '' };

  // Parse as Date (JS converts to local time automatically)
  const dateObj = new Date(utcDateString);

  // Extract local date parts
  // const datePart = dateObj.toISOString().split('T')[0]; // yyyy-MM-dd in UTC - better to get from local Date methods
  // const timePart = dateObj.toTimeString().substring(0, 5); // HH:mm in local time

  // Better to use local methods to get the exact local date & time string:
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
  // Combine date and time
  const [hours, minutes] = timeString.split(':').map(Number);

  // Create Date object in local timezone
  const localDate = new Date(dateString);
  localDate.setHours(hours, minutes, 0, 0);

  // Convert to ISO string in UTC (to send to backend)
  return localDate.toISOString();
}

  onSubmit(): void {
    if (this.codeForm.invalid) {
      this.codeForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.codeForm.value;
    
    // Convert local date/time to UTC ISO string
    const utcDateExpiration = this.convertLocalToUTC(
      formValue.dateExpiration, 
      typeof formValue.timeExpiration === 'string' 
        ? formValue.timeExpiration 
        : `${formValue.timeExpiration.getHours().toString().padStart(2, '0')}:${formValue.timeExpiration.getMinutes().toString().padStart(2, '0')}`
    );

    const codeData = {
      ...formValue,
      dateExpiration: utcDateExpiration
    };

    const operation = this.isEditMode 
      ? this.codePromoService.updateCodePromo(this.data.code.id??0, codeData)
      : this.codePromoService.createCodePromo(codeData);

    operation.subscribe({
      next: () => {
        this.isLoading = false;
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isLoading = false;
        this.errMessage = err.error.message;
        console.error('Error:', err);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}