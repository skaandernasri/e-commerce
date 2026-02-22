import { animate, style, transition, trigger } from '@angular/animations';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { User } from '../../../../core/models/user';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UsersService } from '../../../../core/services/users.service';

@Component({
  selector: 'app-user-modal',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './user-modal.component.html',
  styleUrl: './user-modal.component.css',
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
export class UserModalComponent {
  userForm: FormGroup;
  isEditMode = false;
  isLoading = false;
  selectedRole: string = '';

  constructor(
    public dialogRef: MatDialogRef<UserModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: User },
    private userService: UsersService,
    private fb: FormBuilder
  ) {
    this.userForm = this.fb.group({
      nom: ['', [
        Validators.required, 
        Validators.minLength(4),
        Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/)
      ]],
      prenom: ['', [
        Validators.required, 
        Validators.minLength(4),
        Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/)
      ]],
      telephone: ['', [
        Validators.required, 
        Validators.maxLength(20),
        Validators.minLength(8),
        Validators.pattern(/^\+?\d{1,4}?[-.\s]?\(?\d{1,3}?\)?[-.\s]?\d{1,4}[-.\s]?\d{1,4}[-.\s]?\d{1,9}$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/)
      ]],
      roles: [[], Validators.required],
      isverified: [false, Validators.required],
    });

    if (data.user) {
      this.isEditMode = true;
      this.userForm.patchValue({
        nom: data.user.nom,
        prenom: data.user.prenom,
        telephone: data.user.telephone,
        email: data.user.email,
        isverified: data.user.isverified,
        roles: [...data.user.roles] // Make sure roles is an array
      });
      
      
  }
}

  addRole(): void {
    if (this.selectedRole && !this.userForm.value.roles.includes(this.selectedRole)) {
      this.userForm.patchValue({
        roles: [...this.userForm.value.roles, this.selectedRole]
      });
      this.selectedRole = '';
    }
  }

  removeRole(index: number): void {
    const currentRoles = [...this.userForm.value.roles];
    currentRoles.splice(index, 1);
    this.userForm.patchValue({
      roles: currentRoles
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.userForm.value;
    const userData = {
      ...formValue,
      id: this.isEditMode ? this.data.user.id : undefined
    };

    const operation = this.isEditMode 
      ? this.userService.updateUser(this.data.user.id!, userData)
      : this.userService.createUser(userData);

    operation.subscribe({
      next: () => {
        this.isLoading = false;
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error:', err);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}