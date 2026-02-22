import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '../../../core/models/user';
import { MatDialog } from '@angular/material/dialog';
import { UsersService } from '../../../core/services/users.service';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { UserModalComponent } from './user-modal/user-modal.component';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn } from '@angular/forms';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { EmailService } from '../../../core/services/email.service';
import { SwalService } from '../../../core/services/swal-service.service';

@Component({
  selector: 'app-users-management',
  imports: [CommonModule, FormsModule, PaginationComponent, ReactiveFormsModule],
  templateUrl: './users-management.component.html'
})
export class UsersManagementComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  searchTerm = '';
  isLoading = true;
  errorMessage = '';
  selectedRole: string = 'ALL';
  roles: string[] = ['ALL', 'ADMIN', 'CLIENT', 'GESTIONNAIRE', 'REDACTEUR', 'SUPER_ADMIN'];
  statusMessage: string = '';
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalItems: number = 0;
  emailForm!: FormGroup;
  showEmailForm: boolean = false;
  sendButtonIsLoading = false;

  constructor(
    private userService: UsersService,
    private dialog: MatDialog,
    private fb: FormBuilder,
    private emailService: EmailService,
    private swalService: SwalService
  ) { }

  ngOnInit(): void {
    this.emailForm = this.fb.group({
      to: [''],
      subject: [''],
      text: [''],
      type: ['codePromo'],
      codePromo: [null]
    }, { validators: this.promoCodeRequiredIfTypeIsCodePromo });
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users.map(user => (user.isverified ? user.status = "Verified" : user.status = "Not Verified", user));
        this.filteredUsers = [...users];
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        if (err.status == 404) {
          this.users = [];
          this.filteredUsers = [...this.users];
        } else {
          this.errorMessage = 'SWAL.CART.FAILED_LOAD';
          this.swalService.error(this.errorMessage);
        }
        this.isLoading = false;
      }
    });
  }

  filterUsers() {
    this.filteredUsers = this.users.filter(user =>
      (this.selectedRole != 'ALL' ? user.roles.includes(this.selectedRole) : [...this.users]) &&
      user.email.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
    this.currentPage = 1;
    this.paginatedUsers;
  }

  deleteUser(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete User',
        message: 'Are you sure you want to delete this user?'
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.deleteUser(id).subscribe({
          next: () => {
            this.swalService.success('SWAL.USER.SUCCESS_DELETE');
            this.loadUsers();
          },
          error: (err) => {
            console.error(err);
            this.swalService.error('SWAL.USER.FAILED_DELETE');
          }
        });
      }
    });
  }

  openCreateUser(): void {
    const dialogRef = this.dialog.open(UserModalComponent, {
      data: { user: null },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadUsers();
    });
  }

  editUser(id: number): void {
    const user = this.users.find(c => c.id === id);
    if (user) {
      const dialogRef = this.dialog.open(UserModalComponent, {
        data: { user: user },
        panelClass: ['rounded-lg', 'shadow-xl']
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result) this.loadUsers();
      });
    }
  }

  onPageChange(newPage: number): void {
    this.currentPage = newPage;
  }

  get paginatedUsers() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredUsers.slice(startIndex, startIndex + this.itemsPerPage);
  }

  openEmailForm(user: User): void {
    this.showEmailForm = true;
    this.emailForm.patchValue({ to: user.email });
  }

  closeEmailForm(): void {
    this.showEmailForm = false;
    this.emailForm.reset();
    this.emailForm.get('type')?.setValue('codePromo');
  }

  sendEmail(): void {
    this.sendButtonIsLoading = true;
    if (this.emailForm.valid) {
      if (this.emailForm.value.type == 'codePromo' && this.emailForm.value.codePromo) {
        this.emailForm.value.codePromo = this.emailForm.value.codePromo.toUpperCase();
      }
      this.emailService.sendCodePromoEmail(this.emailForm.value).subscribe({
        next: () => {
          this.closeEmailForm();
          this.swalService.success('SWAL.EMAIL_SENT_SUCCESS');
          this.sendButtonIsLoading = false;
        },
        error: (err) => {
          this.statusMessage = err.error?.message || 'SWAL.ERROR_SENDING_EMAIL';
          this.swalService.error(this.statusMessage);
          console.error(err);
          this.sendButtonIsLoading = false;
        }
      });
    } else {
      this.sendButtonIsLoading = false;
    }
  }

  promoCodeRequiredIfTypeIsCodePromo: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const type = control.get('type')?.value;
    const codePromo = control.get('codePromo')?.value;
    if (type === 'codePromo' && (!codePromo || codePromo.trim() === '')) {
      return { promoCodeRequired: true };
    }
    return null;
  };
}
