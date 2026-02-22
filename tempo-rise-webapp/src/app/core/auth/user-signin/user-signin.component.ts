import { Component, Inject, inject, OnDestroy, OnInit, Optional } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SocialLoginModule } from '@abacritt/angularx-social-login';
import { Subject, switchMap, takeUntil, timer } from 'rxjs';
import { CartService } from '../../services/cart.service';
import { EmailService } from '../../services/email.service';
import { environment } from '../../../../environments/environment';
import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module, ReCaptchaV3Service } from 'ng-recaptcha';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { SwalService } from '../../services/swal-service.service';
import { TranslateModule } from '@ngx-translate/core';
import { OrderService } from '../../services/order.service';
import { UtilisateurAnonymeService } from '../../services/utilisateur-anonyme.service';

declare global {
  const google: any;
}

@Component({
  selector: 'app-user-signin',
  templateUrl: './user-signin.component.html',
  styleUrls: ['./user-signin.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    SocialLoginModule,
    RecaptchaV3Module,
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
export class UserSigninComponent implements OnInit, OnDestroy {
  authService$ = inject(AuthService);
  signinForm: FormGroup;
  isLoading = false;
  sendEmailLoading = false;
  errorMessage: string = '';
  private initialized = false;
  private destroy$ = new Subject<void>();
  private readonly MAX_INIT_ATTEMPTS = 50;
  private readonly INIT_RETRY_INTERVAL = 100;
  private authState = this.authService$.authState;
  showPassword = false;

  constructor(
    @Optional() private dialogRef: MatDialogRef<UserSigninComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private router: Router,
    private cartService: CartService,
    private emailService: EmailService,
    private recaptchaV3Service: ReCaptchaV3Service,
    private swalService: SwalService,
    private orderService: OrderService,
    private anonymeUserService: UtilisateurAnonymeService
  ) {
    this.signinForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      reCaptchaToken: ['']
    });
  }

  ngOnInit(): void {
    if (this.authService$.getCurrentUser.isAuthenticated) {
      this.authService$.clearAuthState();
    }
    this.initializeGoogleSignInWithRetry();
  }

  onSubmit(): void {
    if (this.signinForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.recaptchaV3Service.execute('login').pipe(
      takeUntil(this.destroy$),
      switchMap(token => {
        this.signinForm.get('reCaptchaToken')?.setValue(token);
        return this.authService$.signIn(this.signinForm.value)
      })
    ).subscribe({
      next: (response) => {
        if (this.data?.mergeCart) {
          const userId = response.details?.[3];
          this.handleLoginResponse(userId,);
        }else if(this.data?.mergeOrders) {
          this.mergeOrdersAndLogin();
        }
         else {
          this.router.navigate(['/']);
        }
      },
      error: (err) => {
        console.error(err);
        if (err.code == "4043") this.errorMessage = 'SWAL.INVALID_CREDENTIALS';
        else if(err.code=="4015") this.errorMessage = 'SWAL.ACCOUNT_NOT_VERIFIED';
        else if (err.code="40091")this.errorMessage='SWAL.INVALID_CREDENTIALS';
        else this.errorMessage = err.message || 'SWAL.SIGNIN_FAILED';
        this.isLoading = false;
        this.swalService.error(this.errorMessage);
      }
    });
  }

  sendActivateLink(): void {
    this.sendEmailLoading = true;
    if (this.signinForm.get('email')?.invalid) {
      this.signinForm.get('email')?.markAsTouched();
      this.sendEmailLoading = false;
      return;
    }

    this.emailService.sendVerifEmail({ to: this.signinForm.get('email')?.value }).subscribe({
      next: () => {
this.swalService.success('SWAL.EMAIL_SENT_SUCCESS');
        this.sendEmailLoading = false;
      },
      error: (err) => {
        if (err.error.code === "4043" || err.error.code === "4098") {
          this.swalService.error(err.error.message);
        } else {
this.swalService.error('SWAL.ERROR_SENDING_EMAIL');
        }
        this.sendEmailLoading = false;
      }
    });
  }

  // Google Sign-In Initialization
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
        // Initialize Google Sign-In with custom callback
        google.accounts.id.initialize({
          client_id: environment.googleClientId,
          callback: (response: { credential: string }) => this.handleGoogleCallback(response.credential),
          ux_mode: 'popup'
        });

        // Render the button
        google.accounts.id.renderButton(
          document.getElementById('google-signin-button'),
          {
            type: 'icon',
            shape: 'circle',
            theme: 'filled_blue',
            size: 'large',
            text: 'continue_with',
            width: '100%',
            logo_alignment: 'left'
          }
        );

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

  // Google Sign-In Handler
  private handleGoogleCallback(idToken: string): void {
    this.isLoading = true;
    this.authService$.handleGoogleSignIn(idToken).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => {
        if (response.code === '200') {
          const userId = response.details?.[3];
          
          if (this.data?.mergeCart) {
            this.handleLoginResponse(userId);
          }
          else if(this.data?.mergeOrders) {
            this.mergeOrdersAndLogin();
          }
           else {
            this.router.navigate(['/']);
            //this.cartService.createAndGetCart(userId).subscribe();
          }
        }
      },
      error: (err) => {
        console.error('Google sign-in failed:', err);
        this.showError('SWAL.GOOGLE_SIGNIN_FAILED');
        this.isLoading = false;
      }
    });
  }

  // Shared Login Response Handler (for both regular and Google sign-in)
  private handleLoginResponse(userId: number): void {
    const hasGuestItems = this.cartService.getGuestCart.articles.length > 0;

    if (hasGuestItems) {
      //this.handleMergeCart(userId);
      this.mergeCartsAndLogin(userId);
    } else {
      this.completeLogin(false);
    }
  }
  private mergeCartsAndLogin(userId: number): void {
    this.cartService.mergeCarts(userId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.completeLogin(true);
        this.showSuccess('SWAL.CART.CART_MERGED_SUCCESS');
      },
      error: (err) => {
        console.error("Failed to merge carts", err);
        if (err.error.code === "4096")
          this.showError('SWAL.INSUFFICIENT_STOCK');
        else
          this.showError('SWAL.CART.CART_MERGE_FAILED');
        this.completeLogin(false);
      },
    });
  }
  private mergeOrdersAndLogin():void{
    this.orderService.mergeOrders(this.authState().id!,this.anonymeUserService.id()!).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.completeLogin(true);
        this.showSuccess('SWAL.ORDER.ORDER_MERGED_SUCCESS');
      },
      error: (err) => {
        console.error("Failed to merge orders", err);
        if (err.error.code === "4096")
          this.showError('SWAL.INSUFFICIENT_STOCK');
        else
          this.showError('SWAL.ORDER.ORDER_MERGE_FAILED');
        this.completeLogin(false);
      },
    });
  }

  private completeLogin(isMerged: boolean): void {
    this.isLoading = false;
    if (this.dialogRef) {
      this.dialogRef.close(isMerged);
    }
  }

  closeDialog(): void {
    if (this.dialogRef) {
      this.dialogRef.close();
    }
  }


  private showError(message: string): void {
    this.swalService.error(message);
  }

  showSuccess(message: string): void {
    this.swalService.success(message);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}