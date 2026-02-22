import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { passwordPattern } from '../passwordVerif';
@Component({
  selector: 'app-reset-password',
  templateUrl: './password-reset.component.html',
  imports:[ReactiveFormsModule,RouterLink]
})
export class PasswordResetComponent implements OnInit {
  resetForm = this.fb.group({
     password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(passwordPattern)
      ]],
    confirmPassword: ['', Validators.required]
  }, { 
    validators: this.passwordMatchValidator 
  });

  token: string | null = null;
  isLoading = false;
  tokenValid = false;
  tokenChecked = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
  ) {}

  ngOnInit() {
    
    this.token = this.route.snapshot.queryParamMap.get('token');
    
    if (!this.token) {
      this.errorMessage = 'Invalid reset link';
      this.tokenChecked = true;
      return;
    }
    this.validateToken();
  }

  validateToken() {
    this.http.post(`${environment.apiUrl}/validate-reset-token?token=${this.token}`, 
      { },
    ).subscribe({
      next: () => {
        //this.router.navigate(['/reset-password'], { queryParams: { token: this.token } });
        this.tokenValid = true;
        this.tokenChecked = true;
        this.isLoading = false;
      },
      error: (err) => {
        console.log(err.error);
        this.tokenValid = false;
        this.tokenChecked = true;
        this.errorMessage = 'Invalid or expired token';
        console.log("validate token error"+err);

        this.isLoading = false;
      }
    });
  }

  passwordMatchValidator(form: any) {
    return form.get('password').value === form.get('confirmPassword').value 
      ? null 
      : { mismatch: true };
  }

  onSubmit() {
    if (this.resetForm.invalid || !this.tokenValid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.http.post(`${environment.apiUrl}/reset-password?token=${this.token}`, 
      {
        newPassword: this.resetForm.value.password,
        confirmPassword: this.resetForm.value.confirmPassword
      },
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.successMessage = 'Password reset successfully!';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/auth/user/signin']), 2000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to reset password';
        this.isLoading = false;
      }
    });
  }
}