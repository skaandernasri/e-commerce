import { Component, inject, OnInit } from '@angular/core';
import { ActivateAccountService } from '../../services/activate-account.service';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SwalService } from '../../services/swal-service.service';

@Component({
  selector: 'app-activate-account',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.css']
})
export class ActivateAccountComponent implements OnInit {
  private activateService = inject(ActivateAccountService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private token: string | null = null;

  isLoading = false;
  tokenValid = false;
  tokenChecked = false;
  errorMessage = '';
  successMessage = '';

  constructor(private swalService: SwalService) { }

  ngOnInit() { 
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      this.errorMessage = 'Invalid reset link';
      this.tokenChecked = true;
      return;
    }

    this.isLoading = true;

    this.activateService.activate(this.token).subscribe({
      next: (response) => {
        this.tokenValid = true;
        this.tokenChecked = true;
        this.isLoading = false;

        // Use SwalService instead of MatSnackBar
this.swalService.success('SWAL.ACCOUNT_ACTIVATED');

        // Navigate after showing success
        this.router.navigate(['/auth/user/signin']);
      },
      error: (err) => {
        console.error("Activation error:", err);
        this.tokenValid = false;
        this.tokenChecked = true;
        this.isLoading = false;
        this.errorMessage = 'Invalid or expired token';

        // Optionally show error with SwalService
this.swalService.error('SWAL.INVALID_OR_EXPIRED_TOKEN');
      },
    });
  }
}
