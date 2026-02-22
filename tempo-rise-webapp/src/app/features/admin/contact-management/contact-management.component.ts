import { Component, computed, effect, inject, signal } from '@angular/core';
import { ContactService } from '../../../core/services/contact.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ContactDto } from '../../../core/models/contact';
import { ExpandLongTextComponent } from "../../../shared/components/expand-long-text/expand-long-text.component";
import { ContactModalComponent } from './contact-modal/contact-modal.component';

@Component({
  selector: 'app-contact-management',
  imports: [CommonModule, FormsModule, MatDialogModule, PaginationComponent, ExpandLongTextComponent],
  templateUrl: './contact-management.component.html',
  styleUrl: './contact-management.component.css'
})
export class ContactManagementComponent {
  private contactService=inject(ContactService);
   selectedType = signal('');
   statusContact = signal('');
   refundMethod = signal('');
   sortByDate = signal<'ASC' | 'DESC' | ''>('');
   contacts:ContactDto[]=[]
   isLoading = false;
   totalElements = this.contactService.totalElements;
   totalPages = this.contactService.totalPages;
   pagination = this.contactService.pagination;
  constructor(private dialog: MatDialog) {
    effect(() => {
      this.loadContacts();
    });
    effect(() => {
      if(this.selectedType() != '' || this.statusContact() != '' || this.refundMethod() != '')
        this.resetCurrentPage();
    });
  }
loadContacts(): void {
  
  this.isLoading = true;
      this.contactService.getContact(
        this.pagination().page, 
        this.pagination().size,
        this.selectedType(),
        this.statusContact(),
        this.refundMethod(),
      this.sortByDate()).subscribe({
          next: (response) => {
            this.contacts = response.content;            
          },
          error: (error) => console.error('Error loading contacts:', error),
          complete: () => {
            this.isLoading = false;
          }
        });
  }

  resetCurrentPage(): void {
    this.contactService.resetCurrentPage();
  }
    onPageChange(newPage: number): void {
    this.contactService.updateCurrentPage(newPage);
  }
  updateContact(contactId: number){
    const contact = this.contacts.find(c => c.id === contactId);
    if (contact) {
      const dialogRef = this.dialog.open(ContactModalComponent, {
        data: { contact: contact }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.loadContacts();
        }
      });
    }
  }
   sortOrdersByDate(): void {
    const current = this.sortByDate();
    this.sortByDate.set(current === 'ASC' ? 'DESC' : current === 'DESC' ? 'ASC' : 'DESC');    
  }
}
