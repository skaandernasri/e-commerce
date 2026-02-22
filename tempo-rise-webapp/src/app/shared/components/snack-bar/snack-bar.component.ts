import { Component, Inject, inject } from '@angular/core';
import {
  MAT_SNACK_BAR_DATA,
  MatSnackBarRef,
} from '@angular/material/snack-bar';
import {MatIconModule} from '@angular/material/icon';
import { Product } from '../../../core/models/product';
@Component({
  selector: 'app-snack-bar',
  imports: [MatIconModule],
  styles: `
  /* Success style (keep this if you need both) */
  .success-snackbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #4CAF50;
    font-weight: 500;
    padding: 0 16px;
  }

  /* New failure style */
  .error-snackbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #F44336; /* Material Design red */
    font-weight: 500;
    padding: 0 16px;
    background-color: #FFEBEE; /* Light red background */
    border-left: 4px solid #F44336; /* Accent border */
  }

  .snackbar-content {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .close-button {
    font-weight: 500;
    margin-left: 16px;
  }

  .success-close-button {
    color: #4CAF50;
  }

  .error-close-button {
    color: #F44336;
  }

  .close-button:hover {
    background-color: rgba(244, 67, 54, 0.1);
  }
`,
template: `
  <div class="{{data ? 'success-snackbar' : 'error-snackbar'}}">
    <div class="snackbar-content">
      <mat-icon>{{data ? 'check_circle' : 'error'}}</mat-icon>
      <span matSnackBarLabel>{{data? data.nom +' added successfully to your cart !': 'Error adding product'}}</span>
    </div>
    <button 
      mat-button 
      class="close-button {{data ? 'success-close-button' : 'error-close-button'}}" 
      matSnackBarAction 
      (click)="snackBarRef.dismissWithAction()"
      aria-label="Close"
    >
      CLOSE
    </button>
  </div>
`
})
export class SnackBarComponent {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: Product) { 
  }
  snackBarRef = inject(MatSnackBarRef);
}
