import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  imports: [TranslateModule],
  selector: 'app-confirm-dialog',
  template: `
 <div class="bg-[var(--surface)] p-6 rounded-lg shadow-xl max-w-md w-full">
  <!-- Title -->
  <h2 class="text-xl font-semibold mb-4 text-[var(--text)]">
    @if(data.translate){
   <p> {{ data.title | translate }}</p>
    }
    @else{
   <p> {{ data.title }}</p>
    }
  </h2>

  <!-- Content -->
  <div class="mb-4 text-[var(--text)]">
    @if(data.translate){
    <p>{{ data.message | translate }}</p>
    }
    @else{
    <p>{{ data.message }}</p>
    }
  </div>

  <!-- Actions -->
  <div class="flex justify-end space-x-4">
    <button 
      (click)="onNoClick()" 
      class="px-4 py-2 text-[var(--primary)] hover:bg-[var(--background)] rounded-md transition-colors"
    >
      {{'CONFIRM_DIALOG.NO' | translate}}
    </button>
    <button 
      (click)="onConfirm()" 
      class="px-4 py-2 bg-[var(--primary)] text-white hover:bg-[var(--secondary)] rounded-md transition-colors"
    >
      {{'CONFIRM_DIALOG.YES' | translate}}
    </button>
  </div>
</div>
`,

    //imports: [MatDialogTitle, MatDialogContent, MatDialogActions]

})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { title: string, message: string, translate: boolean }
  ) {}

  onNoClick(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}