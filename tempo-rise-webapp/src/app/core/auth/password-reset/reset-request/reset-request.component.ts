import { Component } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment'; 
import { Router } from '@angular/router';
import { EmailService } from '../../../services/email.service';

@Component({
  selector: 'app-reset-request',
  templateUrl: './reset-request.component.html',
  styleUrls: ['./reset-request.component.css'],
  imports:[FormsModule,ReactiveFormsModule]
})
export class ResetRequestComponent {
  resetForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  isLoading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private emailService:EmailService
  ) {}

  onSubmit() {
    if (this.resetForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.emailService.sendResetPasswordEmail({ to: this.resetForm.get('email')?.value ?? '' }).subscribe({
     
    next: () => {
      this.successMessage = 'Password reset link sent to your email';
      this.isLoading = false;
    },
    error: (err) => {
      this.errorMessage = err.error?.message || 'Failed to send reset link';
      this.isLoading = false;
    }
   })
  }

  navigateToLogin() {
    this.router.navigate(['/auth/user/signin']);
  }
}