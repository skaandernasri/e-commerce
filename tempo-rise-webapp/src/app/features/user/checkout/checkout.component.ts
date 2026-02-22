import { 
  Component, OnInit, OnDestroy, inject, signal, computed, 
  effect
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AdresseResponse } from '../../../core/models/adresse';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { MatRadioModule } from '@angular/material/radio';
import { animate, style, transition, trigger } from '@angular/animations';
import {  Subject, takeUntil } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { OrderService } from '../../../core/services/order.service';
import { CodePromoService } from '../../../core/services/code-promo.service';
import { AdresseService } from '../../../core/services/adresse.service';
import { CountryISO, NgxMaterialIntlTelInputComponent } from 'ngx-material-intl-tel-input';
import { PhoneNumberFormat } from 'google-libphonenumber';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { RecommandedProductsComponent } from "../recommanded-products/recommanded-products.component";
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { SwalService } from '../../../core/services/swal-service.service';
import { TranslateModule } from '@ngx-translate/core';
import { UtilisateurAnonymeService } from '../../../core/services/utilisateur-anonyme.service';
import { ConfigGlobalService } from '../../../core/services/config-global.service';
import { MetaPixelService } from '../../../core/services/meta-pixel.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  templateUrl: './checkout.component.html',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatRadioModule,
    CurrencyFormatPipe,
    NgxMaterialIntlTelInputComponent,
    RecommandedProductsComponent,
    RouterLink,
    RouterModule,
    TranslateModule
  ],
  animations: [
    trigger('expandAnimation', [
      transition(':enter', [
        style({ height: '0', opacity: 0, overflow: 'hidden' }),
        animate('200ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ height: '0', opacity: 0, overflow: 'hidden' }))
      ])
    ])
  ]
})
export class CheckoutComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private adresseService = inject(AdresseService);
  private codePromoService = inject(CodePromoService);
  private orderService = inject(OrderService);
  private destroy$ = new Subject<void>();
  private swalService = inject(SwalService);
  private utilisaterAnonymeService = inject(UtilisateurAnonymeService);
  private globalConfig = inject(ConfigGlobalService);
  private readonly metaPixelService = inject(MetaPixelService);
  configGlobalSignal = this.globalConfig.configGlobal;

  imagesBaseUrl = environment.imageUrlBase;
  authService = inject(AuthService);
  cartService$ = inject(CartService);
  isCartEmpty = computed(() => this.authService.authState().isAuthenticated ? this.cartService$.isEmpty() : this.cartService$.isGuestCartEmpty());
  checkoutForm!: FormGroup; 
  addressForm!: FormGroup;
  editingAddress: AdresseResponse | null = null;
  userId = computed(() => this.authService.authState().isAuthenticated ? this.authService.getUserId : this.utilisaterAnonymeService.id());
  defaultCountry = CountryISO.Tunisia;
  preferredCountries = [CountryISO.France, CountryISO.Tunisia];
  countries = environment.countries.sort((a, b) => a.name.localeCompare(b.name));
  phoneNumberFormat = PhoneNumberFormat.INTERNATIONAL;

  textLabels = {
    mainLabel: 'Téléphone',
    codePlaceholder: 'Code',
    searchPlaceholderLabel: 'Rechercher',
    noEntriesFoundLabel: 'Aucun pays trouvé',
    nationalNumberLabel: 'Numéro',
    hintLabel: 'Sélectionnez un pays et tapez le numéro',
    invalidNumberError: 'Numéro invalide',
    requiredError: 'Ce champ est obligatoire'
  };

  cart = computed(() => this.authService.authState().isAuthenticated ? this.cartService$.cart() : this.cartService$.guestCart());
  subtotal = this.cartService$.subtotal;
  shipping = this.cartService$.shipping;
  total = this.cartService$.total;
  totalWithCodePromo = computed(() => this.promoCodeApplied() ? (this.subtotal() * (1 - this.currentDiscount / 100)) : this.subtotal());
  currentDiscountAmmount = computed(() => this.promoCodeApplied() ? this.subtotal() * (this.currentDiscount / 100) : 0);
  purchaseIsLoading = false;
  isLoading = false;
  promoCodeApplied = signal(false);
  currentDiscount = 0;
  promoCodeError = '';
  promoCodeSuccess = '';

  showAddressForm = false;
  addressFormType: 'LIVRAISON' | 'FACTURATION' = 'LIVRAISON';
  deliveryAddresses: AdresseResponse[] = [];
  billingAddresses: AdresseResponse[] = [];
  selectedDeliveryAddress: AdresseResponse | null = null;
  selectedBillingAddress: AdresseResponse | null = null;
  sameAdresseChecked = true;
  differentAdresseChecked = false;
  livraisonChecked = true;

  constructor(private router: Router, private dialog: MatDialog) {
    effect(() => {
      if(this.cart() && this.cart()!.articles.length > 0){
          this.metaPixelService.addInitiateCheckoutEvent(
            this.cart()!.articles.map((item) => item.id.toString()),
            this.cart()!.articles.map((item) => {
              return {
                id: item.id.toString(),
                quantity: item.variants.map((v) => v.cartQuantity).reduce((a, b) => a + b, 0),
              }
            }),
            'TND',
            this.cart()!.articles.flatMap((item)=>item.variants).length,
            this.total()
          )
      }
    })
  }

  ngOnInit(): void {    
    this.initForm();
    this.loadCart();
    this.loadAddresses();
    this.updateFormWithMainDataStorage();
  }
  updateFormWithMainDataStorage() {
    if(localStorage.getItem('checkout-form-details') && !this.authService.authState().isAuthenticated){
      this.checkoutForm.patchValue({
        prenom: JSON.parse(localStorage.getItem('checkout-form-details')!).prenom,
        nom: JSON.parse(localStorage.getItem('checkout-form-details')!).nom,
        email: JSON.parse(localStorage.getItem('checkout-form-details')!).email,
        telephone: JSON.parse(localStorage.getItem('checkout-form-details')!).telephone,
        modePaiement: JSON.parse(localStorage.getItem('checkout-form-details')!).modePaiement,
        legal_conditions: JSON.parse(localStorage.getItem('checkout-form-details')!).legal_conditions
      });
    }
  }
  updateFormWithAdressesStorage() {
    if(localStorage.getItem('checkout-form-details') && !this.authService.authState().isAuthenticated){
      const deliveryAddress = JSON.parse(localStorage.getItem('checkout-form-details')!).DeliveryAddress;
      const billingAddress = JSON.parse(localStorage.getItem('checkout-form-details')!).BillingAddress;
      const sameAdresse = JSON.parse(localStorage.getItem('checkout-form-details')!).sameAdresse;
      this.selectDeliveryAddress(deliveryAddress);
      if(!sameAdresse){
        this.differentAdresseChecked = true;
        this.sameAdresseChecked = false;
        this.selectBillingAddress(billingAddress);
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    const currentUser = this.authService.getCurrentUser;

    this.addressForm = this.fb.group({
      ligne1: ['', Validators.required],
      ligne2: [''],
      ville: ['', Validators.required],
      codePostal: ['', Validators.required],
      pays: ['', Validators.required]
    });

    this.checkoutForm = this.fb.group({
      prenom: [currentUser.username?.split(' ')[0]==='null'? '' : currentUser.username?.split(' ')[0] || '', [Validators.required, Validators.minLength(2), Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/)]],
      nom: [currentUser.username?.split(' ')[1]==='null'? '' : currentUser.username?.split(' ')[1] || '', [Validators.required, Validators.minLength(2) , Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/)]],
      email: [currentUser.email, [Validators.required, Validators.email]],
      telephone: ['', [Validators.required]],
      modePaiement: ['A_LIVRAISON', Validators.required],
      legal_conditions: [false, Validators.requiredTrue],
      sameAdresse: [true],
      produits: this.fb.array([]),
      DeliveryAddress: this.fb.group({    
        id: [null],
        ligne1: ['', Validators.required],
        ligne2: [''],
        ville: ['', Validators.required],
        codePostal: ['', Validators.required],
        pays: ['', Validators.required]
      }),
      BillingAddress: this.fb.group({
        id: [null],
        ligne1: ['', Validators.required],
        ligne2: [''],
        ville: ['', Validators.required],
        codePostal: ['', Validators.required],
        pays: ['', Validators.required]
      }),
      promoCode: ['']
    });
  }

  private loadCart() {
    if(this.authService.authState().isAuthenticated)
      this.cartService$.createAndGetCart(this.userId()!).subscribe();
  }

  private loadAddresses() {
    this.adresseService.getAdresseByUserId(this.userId()!).subscribe(data => {
      this.deliveryAddresses = data.filter(a => a.type === 'LIVRAISON');
      this.billingAddresses = data.filter(a => a.type === 'FACTURATION');
      this.updateFormWithAdressesStorage();
    });
  }

  applyPromoCode() {
    const promoCode = this.checkoutForm.get('promoCode')?.value;
    if (!promoCode) return this.showError('Please enter a promo code');

    this.codePromoService.getByCode(promoCode).subscribe({
      next: (res) => {
        if (new Date(res.dateExpiration) < new Date()) {
          return this.showError('Promo code expired');
        }
        this.promoCodeApplied.set(true);
        this.currentDiscount = res.reduction;
        
        this.promoCodeSuccess = `Promo code applied! ${res.reduction}% discount`;
        this.checkoutForm.get('promoCode')?.setValue(res.code);
        this.checkoutForm.get('promoCode')?.disable();
        this.swalService.success('SWAL.PROMOTION.PROMOCODE_APPLIED');
      },
      error: () => this.showError('Invalid or expired promo code')
    });
  }

  private showError(message: string) {
    this.promoCodeError = message;
    this.promoCodeSuccess = '';
    this.promoCodeApplied.set(false);
    this.currentDiscount = 0;
    this.swalService.error(message);
  }

  checkSameAdresse(): void {
    this.selectedBillingAddress = null;
    this.sameAdresseChecked = true;
    this.differentAdresseChecked = false;
    const delivery = this.checkoutForm.get('DeliveryAddress')?.value;
    this.checkoutForm.get('BillingAddress')?.patchValue(delivery);
    this.checkoutForm.get('BillingAddress')?.setErrors(null);
    this.checkoutForm.get('sameAdresse')?.setValue(true);
  }

  checkDifferentAdresse(): void {
    if(this.selectedBillingAddress) return;
    this.sameAdresseChecked = false;
    this.differentAdresseChecked = true;
    this.selectedBillingAddress = null;
    this.checkoutForm.get('BillingAddress')?.patchValue(null);
    this.checkoutForm.get('BillingAddress')?.setErrors({ required: true });
    this.checkoutForm.get('sameAdresse')?.setValue(false);    
  }

  checkmodePaiement(modePaiement: string): void {
    this.livraisonChecked = modePaiement === 'A_LIVRAISON';
    this.checkoutForm.get('modePaiement')?.patchValue(modePaiement);
  }

  selectDeliveryAddress(deliveryAddress: AdresseResponse) {
    this.selectedDeliveryAddress = this.deliveryAddresses.find(a => a.id === deliveryAddress.id) || null;
    if (this.selectedDeliveryAddress) {
      this.checkoutForm.patchValue({ DeliveryAddress: this.selectedDeliveryAddress });
    }
    if(this.sameAdresseChecked) {
      this.checkoutForm.get('BillingAddress')?.patchValue(this.selectedDeliveryAddress);
    }
  }

  selectBillingAddress(billingAddress: AdresseResponse) {
    this.selectedBillingAddress = this.billingAddresses.find(a => a.id === billingAddress.id) || null;
    if (this.selectedBillingAddress) {
      this.checkoutForm.patchValue({ BillingAddress: this.selectedBillingAddress });
    }
  }

  allowOnlyNumbers(event: KeyboardEvent): void {
    const allowed = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab'];
    if (!/\d/.test(event.key) && !allowed.includes(event.key)) event.preventDefault();
  }

 markFormAsTouched(formGroup = this.checkoutForm) {
  Object.values(formGroup.controls).forEach(control => {
    if (control instanceof FormGroup) {
      this.markFormAsTouched(control);
    } else {
      control.markAsTouched();
      control.updateValueAndValidity();
    }
  });
}


  logFormErrors(form: FormGroup): void {
    Object.entries(form.controls).forEach(([key, control]) => {
      if (control instanceof FormGroup) this.logFormErrors(control);
      else if (control.errors) console.error(`Control: ${key}`, control.errors);
    });
  }

 onSubmit(): void {
  this.purchaseIsLoading = true;
  if (!this.differentAdresseChecked) this.checkSameAdresse();
  if (this.checkoutForm.invalid || this.cart().articles.length === 0) {
    this.logFormErrors(this.checkoutForm);
    this.markFormAsTouched();
    this.swalService.error('SWAL.CART.INVALID_FORM_OR_EMPTY_CART');
    this.purchaseIsLoading = false;
    return;
  }

  const formData = {
    utilisateurId: this.userId()!,
    nom: this.checkoutForm.get('nom')?.value,
    prenom: this.checkoutForm.get('prenom')?.value,
    email: this.checkoutForm.get('email')?.value,
    telephone: this.checkoutForm.get('telephone')?.value,
    codePromo: this.checkoutForm.get('promoCode')?.value || null,
    token: 'TND',
    total: this.totalWithCodePromo(),
    adresseLivraisonId: this.checkoutForm.get('DeliveryAddress')?.value?.id,
    adresseFacturationId: this.checkoutForm.get('BillingAddress')?.value?.id,
    modePaiement: this.checkoutForm.get('modePaiement')?.value,
    produits: this.cart().articles,
    shipping : this.shipping()
  };

  const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    data: {
      title: 'SWAL.CHECKOUT_CONFIRM_TITLE',
      message: 'SWAL.CHECKOUT_CONFIRM_MESSAGE',
      translate: true
    }
  });

  dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirm => {
    if (!confirm) {
      this.purchaseIsLoading = false;
      return;
    }
if(this.authService.authState().isAuthenticated){
   this.orderService.createOrder(formData)
  .pipe(takeUntil(this.destroy$))
  .subscribe({
    next: (res) => {
      // Clear the cart BEFORE redirect or navigate
      this.cartService$.removeAllItemsFromCart(this.userId()!)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.swalService.success('SWAL.ORDER_CREATED');
            
            // Then redirect or navigate
            if (res.payUrl && res.paymentRef) {
              window.location.href = res.payUrl;
            } else {
              this.router.navigate(['/paymentDetails', Number(res.paymentRef)]);
            }
          },
          error: (err) => this.swalService.error(err.error?.message ||'SWAL.CART.FAILED_UPDATE')
        });
      this.purchaseIsLoading = false;
    },
    error: (err) => {
      console.error(err);
      this.swalService.error(err.error?.message || 'SWAL.CART.FAILED_CREATE');
      this.purchaseIsLoading = false;
    }
  });
  }
  else{
    this.orderService.guestCreateOrder(formData).subscribe({
      next: (res) => {
        this.cartService$.removeAllItemsFromGuestCart();
        this.swalService.success('SWAL.ORDER_CREATED');
        localStorage.setItem('checkout-form-details', JSON.stringify(this.checkoutForm.value));
        if (res.payUrl && res.paymentRef) {
          window.location.href = res.payUrl;
        } else {
          this.router.navigate(['/paymentDetails', Number(res.paymentRef)]);
        }
        this.purchaseIsLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.swalService.error(err.error?.message || 'SWAL.CART.FAILED_CREATE');
        this.purchaseIsLoading = false;
      }
    })
  }
})
  
  }


  openAddressForm(type: 'LIVRAISON' | 'FACTURATION'): void {
    this.addressFormType = type;
    this.showAddressForm = true;
    this.addressForm.get('pays')?.setValue('Tunisia');
  }

  closeAddressForm(): void {
    this.showAddressForm = false;
    this.editingAddress = null;
    this.addressForm.reset();
  }

  submitAddress(): void {
    if (this.addressForm.invalid) return;

    const payload = { ...this.addressForm.value, type: this.addressFormType, utilisateurId: this.userId() };
    const req$ = this.editingAddress ? this.adresseService.updateAdresse(this.editingAddress.id, payload) : this.adresseService.createAdresse(payload);

    req$.subscribe({
      next: () => {
        this.loadAddresses();
        this.closeAddressForm();
        this.swalService.success(this.editingAddress ? 'SWAL.ADDRESS.SUCCESS_UPDATE' : 'SWAL.ADDRESS.SUCCESS_CREATE');
      },
      error: () => this.swalService.error(this.editingAddress ? 'SWAL.ADDRESS.FAILED_UPDATE' : 'SWAL.ADDRESS.FAILED_CREATE')
    });
  }
}
