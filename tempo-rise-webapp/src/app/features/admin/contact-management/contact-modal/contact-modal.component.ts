import { animate, style, transition, trigger } from '@angular/animations';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ContactDto, ContactStatusUpdateDto } from '../../../../core/models/contact';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ContactService } from '../../../../core/services/contact.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contact-modal',
  imports: [CommonModule, FormsModule,ReactiveFormsModule],
  templateUrl: './contact-modal.component.html',
  styleUrl: './contact-modal.component.css',
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
export class ContactModalComponent {
    contactForm: FormGroup;
    contact?: ContactStatusUpdateDto;
    isLoading = false;

    constructor(
    public dialogRef: MatDialogRef<ContactModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { contact: ContactDto },
    private contactService: ContactService,
    private fb: FormBuilder

  ) {
    this.contactForm = this.fb.group({
      statusContact: [data.contact.statusContact, [Validators.required]],
      refundMethod: [data.contact.refundMethod??'VIREMENT'],
      isRefunded: [data.contact.isRefunded??false],
      adminResponse: ['']
    });
  }
  onSubmit(): void {
    if(this.contactForm.valid) {
      this.isLoading = true;
      this.contactService.updateContact(this.data.contact.id, this.contactForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.dialogRef.close(true);
        },
        error: (err) => {
          console.error(" error in contact",err);
          
          this.isLoading = false;
        }
      });
    }
  }
onCancel(): void {
    this.dialogRef.close(false);
  }


}
