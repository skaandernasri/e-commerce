import { Component, OnDestroy, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { SocialLoginModule } from '@abacritt/angularx-social-login';
import { last, Subject, switchMap, takeUntil, timer } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module, ReCaptchaV3Service } from 'ng-recaptcha';
import { CommonModule } from '@angular/common';
import { SwalService } from '../../services/swal-service.service';
import { TranslateModule } from '@ngx-translate/core';
import { passwordPattern } from '../passwordVerif';

@Component({
  selector: 'app-user-signup',
  templateUrl: './user-signup.component.html',
  styleUrls: ['./user-signup.component.css'],
  imports: [RouterLink, FormsModule, ReactiveFormsModule,
    SocialLoginModule, RecaptchaV3Module, CommonModule,
    TranslateModule
  ],
  providers: [
    ReCaptchaV3Service,
    {
      provide: RECAPTCHA_V3_SITE_KEY,
      useValue: environment.recaptchaSiteKey
    }
  ]
})
export class UserSignupComponent implements OnInit, OnDestroy {
  signupForm: FormGroup;
  errorMessage: string = '';
  theme: 'light' | 'dark' | 'colorful' = 'light';
  isLoading = false;
  progress = 0;
  showSuccess = false;
  showPassword = false;
  showConfirmPassword = false;
  destroy$ = new Subject<void>();
  private initialized = false;
  private readonly MAX_INIT_ATTEMPTS = 50;
  private readonly INIT_RETRY_INTERVAL = 100;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private recaptchaV3Service: ReCaptchaV3Service,
    private swalService: SwalService
  ) {
    this.signupForm = this.fb.group({
      email: this.fb.control('', [
        Validators.required,
        Validators.email,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/)
      ]),
      roles: this.fb.control(['CLIENT']),
      password: this.fb.control('', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(passwordPattern)
      ]),
      confirmPassword: this.fb.control('', [
        Validators.required,
        this.matchValues('password')
      ]),
      legal_conditions: [false, Validators.requiredTrue],
      reCaptchaToken: this.fb.control(''),
      dateNaissance: this.fb.control('', [
        Validators.required,
        this.ageValidator(18)
      ]),
      genre: this.fb.control('', Validators.required)
    });
  }

  ngOnInit(): void {
    if (this.authService.getCurrentUser.isAuthenticated) {
      this.authService.clearAuthState();
    }
    this.initializeGoogleSignInWithRetry();
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return};

    this.isLoading = true;
    this.simulateProgress();

    this.recaptchaV3Service.execute('login').pipe(
      takeUntil(this.destroy$),
      switchMap(token => {
        this.signupForm.get('reCaptchaToken')?.setValue(token);
        return this.authService.signUp(this.signupForm.value);
      })
    ).subscribe({
      next: () => {
        this.isLoading = false;
        this.showSuccess = true;
        this.errorMessage = '';
        this.swalService.success('Signup successful, we have sent you a verification email. Check your inbox.');
        setTimeout(() => this.router.navigate(['/auth/user/signin']), 3000);
      },
      error: (err) => {
        this.isLoading = false;        
        if(err.error.code == '4090') {
          this.errorMessage = 'SIGNUP.EMAIL_ALREADY_EXISTS';
        }
        else
        {
          this.errorMessage = err.error.message || 'SIGNUP.FAILED';
        }        
        console.error(err);
        this.swalService.error(this.errorMessage);
      }
    });
  }

  matchValues(matchTo: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const parent = control.parent;
      if (!parent) return null;

      const matchingControl = parent.get(matchTo);
      if (!matchingControl) return null;

      return control.value === matchingControl.value
        ? null
        : { passwordMismatch: true };
    };
  }

  ageValidator(minAge: number): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (!control.value) return null;
      const birthDate = new Date(control.value);
      const today = new Date();
      let age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) age--;
      return age >= minAge ? null : { underage: { value: control.value } };
    };
  }

  simulateProgress() {
    const interval = setInterval(() => {
      this.progress += Math.random() * 10;
      if (this.progress >= 100) clearInterval(interval);
    }, 200);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeGoogleSignInWithRetry(): void {
    timer(0, this.INIT_RETRY_INTERVAL).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (attempt) => {
        if (this.initialized || attempt >= this.MAX_INIT_ATTEMPTS) {
          this.handleGoogleInitCompletion(attempt);
          this.destroy$.next();
          return;
        }
        this.tryInitializeGoogleButton();
      }
    });
  }

  private tryInitializeGoogleButton(): void {
    try {
      if (typeof google !== 'undefined' && google.accounts?.id) {
        this.authService.renderGoogleButton('google-signin-button');
        this.initialized = true;
      }
    } catch (error) {
      console.error('Error initializing Google button:', error);
    }
  }

  private handleGoogleInitCompletion(attempt: number): void {
    if (attempt >= this.MAX_INIT_ATTEMPTS && !this.initialized) {
      console.error('Google Sign-In failed to initialize after maximum attempts');
    }
  }
}
