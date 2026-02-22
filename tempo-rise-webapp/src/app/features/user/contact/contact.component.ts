import { Component, computed, effect, inject, Signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { Router } from '@angular/router';
import { OrderResponse } from '../../../core/models/order';
import { CommonModule } from '@angular/common';
import { ContactService } from '../../../core/services/contact.service';
import { SwalService } from '../../../core/services/swal-service.service';
import { TranslateModule } from '@ngx-translate/core';
import { ParamsService } from '../../../core/services/params.service';

interface SociatyInfo {
  telephone: string;
  adressMail: string;
}
@Component({
  selector: 'app-contact',
  imports: [CommonModule, ReactiveFormsModule,TranslateModule],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent {
  private authService = inject(AuthService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private contactService = inject(ContactService);
  private swalService = inject(SwalService);
  private paramService = inject(ParamsService);
  
  activeContactSections = computed(() => this.paramService.contactPageSections().filter(section => section.active));
  activeSocietyInfoSection = computed(() => this.activeContactSections().find(section => section.type === 'SOCIATY_INFO'));
  contactForm!: FormGroup;
  authState= this.authService.authState;
  isAuthenticated = this.authService.isAuthenticated;
  userId = this.authService.getUserId;
  commandes: OrderResponse[] = [];
  sendButtonIsLoading = false;

  sociatyInfoContent: Signal<SociatyInfo | null> = computed(() =>{
    const content = this.activeSocietyInfoSection()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        telephone: parsed.telephone,
        adressMail: parsed.adressMail
      };
    }
    return null; 
  });
  constructor(private fb: FormBuilder) {
        this.contactForm = this.fb.group({
      subject: ['', Validators.required],
      message: ['', Validators.required],
      type: ['AIDE', Validators.required],
      email: ['', [Validators.email]],
      commandeId: [null],
    }); 
    effect(() => {
      if(this.authState().id){
        this.contactForm.get('email')?.setValue(this.authState().email);
        this.contactForm.get('email')?.removeValidators([Validators.required]);
        this.contactForm.get('email')?.updateValueAndValidity();
      }
      else{
        this.contactForm.get('email')?.addValidators([Validators.required]);
        this.contactForm.get('email')?.updateValueAndValidity();
      }
    })
  }

  ngOnInit(): void {

    this.contactForm.get('type')?.valueChanges.subscribe(type => {
      if (type === 'AIDE') {
        this.contactForm.get('commandeId')?.disable();
      } else {
        this.contactForm.get('commandeId')?.enable();
        this.orderService.getOrderByUserId(this.userId!).subscribe(data => {
          this.commandes = data.filter(commande =>
            commande.paiements.some(paiement => paiement.status === 'completed') || commande.statut === 'LIVREE'
          );
        });
      }
    });

    if (this.contactForm.get('type')?.value === 'AIDE') {
      this.contactForm.get('commandeId')?.disable();
    }
    this.paramService.getSectionsByPageType('CONTACT').subscribe();
  }

  submit(): void {
    if (this.contactForm.invalid) {
      this.swalService.error('SWAL.CONTACT.VALID_FIELDS');
      return;
    }

    this.sendButtonIsLoading = true;
    const formData = {
      ...this.contactForm.value,
      userId: this.authState().id,
      commandeId: this.contactForm.value.type === 'RETOUR'
        ? this.contactForm.value.commandeId
        : null,
    };

    this.contactService.createContact(formData).subscribe({
      next: () => {
        this.swalService.success('SWAL.CONTACT.SUCCESS_SEND');
        this.contactForm.reset({ type: 'AIDE' });
        this.contactForm.get('commandeId')?.disable();
        this.sendButtonIsLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.swalService.error(err.error?.message || 'SWAL.CONTACT.FAILED_SEND');
        this.sendButtonIsLoading = false;
      },
      complete: () => {
        this.sendButtonIsLoading = false;
      }
    });
  }

  redirectToLogin() {
    this.router.navigate(['/auth/user/signin']);
  }
}
