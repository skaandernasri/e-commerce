import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CartService } from '../../services/cart.service';
import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module, ReCaptchaV3Service } from 'ng-recaptcha';
import { environment } from '../../../../environments/environment';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { passwordPattern } from '../passwordVerif';

@Component({
  selector: 'app-admin-signin',
  templateUrl: './admin-signin.component.html',
  styleUrls: ['./admin-signin.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RecaptchaV3Module
    //RouterLink,
  ],
  providers: [
      ReCaptchaV3Service,
      {
      provide: RECAPTCHA_V3_SITE_KEY,
      useValue: environment.recaptchaSiteKey
      }
    ]
})
export class AdminSigninComponent implements OnInit, OnDestroy {
  authService$=inject(AuthService)
  signinForm: FormGroup;
  errorMessage: string = '';
  authState = this.authService.authState;
  private destroy$ = new Subject<void>();
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cartService:CartService,
    private recaptchaV3Service: ReCaptchaV3Service
  ) {
    this.signinForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]
    ],
      password: ['',[Validators.minLength(8),
        Validators.pattern(passwordPattern)]
      ],
      reCaptchaToken: ['']
      });
  }
  ngOnInit(): void {
          if (this.authService$.getCurrentUser.isAuthenticated) {
      this.authService$.clearAuthState();
       }
  }
  onSubmit(): void {
    if (this.signinForm.invalid) return;
        this.isLoading = true;
    this.recaptchaV3Service.execute('login').pipe(
           takeUntil(this.destroy$),
           switchMap(token => {
            this.signinForm.get('reCaptchaToken')?.setValue(token);
            return this.authService$.signIn(this.signinForm.value)})
           ).subscribe({
             next: () => {
                  this.isLoading = false;

              this.router.navigate(['/admin']);
              this.cartService.createAndGetCart(this.authState().id!).subscribe();
             },
              error: (err) => {
                    this.isLoading = false;

          this.errorMessage = err.error.message || 'Admin signin failed. Please try again.';
        }
           });  
  }
    ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}