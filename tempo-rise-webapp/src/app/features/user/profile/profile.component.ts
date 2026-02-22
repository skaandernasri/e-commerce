import { Component, ElementRef, inject, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { AdresseService } from '../../../core/services/adresse.service';
import { AuthService } from '../../../core/services/auth.service';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { AdresseResponse } from '../../../core/models/adresse';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';
import { UsersService } from '../../../core/services/users.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { DownloadPdfService } from '../../../core/services/download-pdf.service';
import { InvoiceTemplateComponent } from "../../../shared/components/invoice-template/invoice-template.component";
import { SwalService } from '../../../core/services/swal-service.service';
import { TranslateModule } from '@ngx-translate/core';
import { FactureService } from '../../../core/services/facture.service';
import { FactureResponse } from '../../../core/models/facture';

@Component({
  selector: 'app-profile',
  standalone: true,
  templateUrl: './profile.component.html',
  imports: [CommonModule, ReactiveFormsModule, InvoiceTemplateComponent,TranslateModule]
})
export class ProfileComponent implements OnInit {
  private authService = inject(AuthService);
  private pdfService = inject(DownloadPdfService);
  private adresseService = inject(AdresseService);
  private userService = inject(UsersService);
  private sanitizer = inject(DomSanitizer);
  private fb = inject(FormBuilder);
  private swalService = inject(SwalService);

  authState = this.authService.authState;
  downloadingfactureId = this.pdfService.downloadingfactureId;
  factures: FactureResponse[] = [];
  adresseLivraison: AdresseResponse[] = [];
  adresseFacturation: AdresseResponse[] = [];
  countries = environment.countries.sort((a, b) => a.name.localeCompare(b.name));

  profileForm: FormGroup;
  saveChangesIsLoading = false;
  showAddressForm = false;
  addressForm: FormGroup;
  editingAddress: AdresseResponse | null = null;
  addressFormType: 'LIVRAISON' | 'FACTURATION' = 'LIVRAISON';

  @ViewChildren('invoiceTemplate') invoiceTemplates!: QueryList<InvoiceTemplateComponent>;
  @ViewChild('fileInput') fileInput!: ElementRef;
  selectedFile: File | null = null;
  uploadProgress = 0;
  imageBaseUrl = environment.imageUrlBase;
  profileImageUrl?: string | SafeUrl;
  constructor(private factureService:FactureService) {
    this.profileForm = this.fb.group({
      id: [null],
      nom: ['', [Validators.required, Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/), Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/), Validators.minLength(4)]],
      telephone: ['', [
        Validators.required, Validators.maxLength(20), Validators.minLength(8),
        Validators.pattern(/^\+?\d{1,4}?[-.\s]?\(?\d{1,3}?\)?[-.\s]?\d{1,4}[-.\s]?\d{1,4}[-.\s]?\d{1,9}$/)
      ]],
      dateNaissance: this.fb.control('', [Validators.required, this.ageValidator(18)]),
      genre: this.fb.control('', Validators.required)
    });

    this.addressForm = this.fb.group({
      ligne1: ['', Validators.required],
      ligne2: [''],
      ville: ['', Validators.required],
      codePostal: ['', Validators.required],
      pays: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadOrders();
    this.loadAdresses();
  }

  downloadPdf(facture: FactureResponse) {
    const matchingComponent = this.invoiceTemplates.find(cmp => cmp.facture?.id === facture.id);
    if (!matchingComponent || !matchingComponent.invoiceElement) {
      this.swalService.error('SWAL.INVOICE.ELEMENT_NOT_FOUND');
      return;
    }
    this.pdfService.downloadPDF(matchingComponent.invoiceElement, facture, 'facture');
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.profileImageUrl = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file));
    }
  }

  uploadImage(): void {
    if (!this.selectedFile) return;

    const interval = setInterval(() => {
      this.uploadProgress += 10;
      if (this.uploadProgress >= 100) {
        clearInterval(interval);
        setTimeout(() => {
          this.saveImagePathToDatabase(this.selectedFile!);
          this.uploadProgress = 0;
          this.selectedFile = null;
        }, 500);
      }
    }, 200);
  }

  private saveImagePathToDatabase(file: File): void {
    if (!file) return;

    const formData = new FormData();
    formData.append('image', file);
    this.userService.updateUserProfileImage(this.authState().id!, formData).subscribe({
      next: (response) => {
        this.profileImageUrl = this.imageBaseUrl + response.imageUrl;
        this.swalService.success('SWAL.PROFILE.IMAGE_UPDATE_SUCCESS')},
      error: (err) => {
        console.error('Image upload failed', err);
        this.swalService.error('SWAL.PROFILE.IMAGE_UPDATE_FAILED');
      }
    });
  }


  getInitials(): string {
    const firstName = this.profileForm.get('prenom')?.value || '';
    const lastName = this.profileForm.get('nom')?.value || '';
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  onlyNumbers(event: any) {
    const charCode = event.which ? event.which : event.keyCode;
    return !(charCode > 31 && (charCode < 48 || charCode > 57));
  }

  loadCurrentUser() {
    this.userService.getUserById(this.authState().id!).subscribe({
      next: (data) => {this.profileForm.patchValue(data)
        this.profileImageUrl = data.imageUrl;

      },
      error: (err) => this.swalService.error('SWAL.USER.FAILED_LOAD')
    });
  }

  loadOrders() {
    this.factureService.getFactureByUserId(this.authState().id!).subscribe({
      next: (data) => {this.factures = data;        
      },
      error: () => this.swalService.error('SWAL.ORDERS.FAILED_LOAD')
    });
  }

  loadAdresses() {
    this.adresseService.getAdresseByUserId(this.authState().id!).subscribe({
      next: (data) => {
        this.adresseFacturation = data.filter(a => a.type === 'FACTURATION');
        this.adresseLivraison = data.filter(a => a.type === 'LIVRAISON');
      },
      error: () => this.swalService.error('SWAL.ADDRESS.FAILED_LOAD')
    });
  }

  onSubmit() {
    if (this.profileForm.invalid) return;
    this.saveChangesIsLoading = true;
    this.profileForm.get('id')?.setValue(this.authState().id);

    this.userService.updateUserProfile(this.profileForm.value).subscribe({
      next: () => this.swalService.success('SWAL.PROFILE.UPDATE_SUCCESS'),
      error: (err) => this.swalService.error(err.error?.message || 'SWAL.PROFILE.UPDATE_FAILED'),
      complete: () => this.saveChangesIsLoading = false
    });
  }

  openAddressForm(type: 'LIVRAISON' | 'FACTURATION', address?: AdresseResponse) {
    this.addressFormType = type;
    this.editingAddress = address || null;

    if (address) {
      this.addressForm.patchValue(address);
    } else {
      this.addressForm.reset();
    }

    this.showAddressForm = true;
  }

  closeAddressForm() {
    this.showAddressForm = false;
    this.editingAddress = null;
    this.addressForm.reset();
  }

  submitAddress() {
    if (this.addressForm.invalid) return;

    const addressData = {
      ...this.addressForm.value,
      type: this.addressFormType,
      utilisateurId: this.authState().id
    };

    const request$ = this.editingAddress
      ? this.adresseService.updateAdresse(this.editingAddress.id, addressData)
      : this.adresseService.createAdresse(addressData);

    request$.subscribe({
      next: () => {
        this.loadAdresses();
        this.closeAddressForm();
        this.swalService.success(this.editingAddress ? 'SWAL.ADDRESS.SUCCESS_UPDATE' : 'SWAL.ADDRESS.SUCCESS_CREATE');
      },
      error: () => this.swalService.error(this.editingAddress ? 'SWAL.ADDRESS.FAILED_UPDATE' : 'SWAL.ADDRESS.FAILED_CREATE')
    });
  }

  deleteAddress(id: number) {
    this.swalService.modal({
      title: 'Confirm Deletion',
      text: 'Are you sure you want to delete this address?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'Cancel'
    }).then((result) => {
      if (result.isConfirmed) {
        this.adresseService.deleteAdresses(id).subscribe({
          next: () => {
            this.loadAdresses();
            this.swalService.success('SWAL.ADDRESS.SUCCESS_DELETE');
          },
          error: (err) => {
            console.error(err);
            if(String(err.error?.message).includes("(save the transient instance before flushing)"))
              this.swalService.error('SWAL.ADDRESS.FAILED_DELETE_LINKED_ORDER');
            else
              this.swalService.error('SWAL.ADDRESS.FAILED_DELETE')}
        });
      }
    });
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
}
