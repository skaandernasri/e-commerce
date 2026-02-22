import { Component, computed, ElementRef, inject, Input, OnInit, signal, ViewChild } from '@angular/core';
import { OrderService } from '../../../../core/services/order.service';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerForm, OrderResponse } from '../../../../core/models/order';
import { CommonModule } from '@angular/common';
import { Product, variantResponse } from '../../../../core/models/product';
import { CurrencyFormatPipe } from '../../../../core/pipe/currency-pipe.pipe';
import { DownloadPdfService } from '../../../../core/services/download-pdf.service';
import { InvoiceTemplateComponent } from "../../../../shared/components/invoice-template/invoice-template.component";
import { FormBuilder, FormGroup, FormsModule, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { SwalService } from '../../../../core/services/swal-service.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { FactureResponse } from '../../../../core/models/facture';
import { FactureService } from '../../../../core/services/facture.service';
import { Observable, tap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { environment } from '../../../../../environments/environment';
import { AdresseResponse } from '../../../../core/models/adresse';
import { AdresseService } from '../../../../core/services/adresse.service';
import { CountryISO, NgxMaterialIntlTelInputComponent } from 'ngx-material-intl-tel-input';
import { PhoneNumberFormat } from 'google-libphonenumber';
import { ProductService } from '../../../../core/services/product.service';
import { SpinnerComponent } from "../../../../shared/components/spinner/spinner.component";
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { UserSigninComponent } from '../../../../core/auth/user-signin/user-signin.component';
import { ConfigGlobalService } from '../../../../core/services/config-global.service';

@Component({
  selector: 'app-order-details',
  imports: [CommonModule, InvoiceTemplateComponent, CurrencyFormatPipe, FormsModule,
    TranslateModule, ReactiveFormsModule, NgxMaterialIntlTelInputComponent, SpinnerComponent],
  templateUrl: './order-details.component.html',
  styleUrl: './order-details.component.css'
})
export class OrderDetailsComponent implements OnInit {
  private pdfService = inject(DownloadPdfService);
  private swalService = inject(SwalService);
  private authService = inject(AuthService);
  private globalConfig = inject(ConfigGlobalService);
  
  // FIXED: Store the signal itself, not the called value
  authState = this.authService.authState;
  
  imagesBaseUrl = environment.imageUrlBase;
  @Input() hideStatus = false;
  
  // FIXED: If these are signals, store them as signals
  // If they're methods that return signals, store the signal result
  isAdmin = this.authService.isAdmin;  // Don't call it
  isLoggedIn = this.authService.isLoggedIn;  // Don't call it
  
  downloadingfactureId = this.pdfService.downloadingfactureId;
  
  // FIXED: Use computed signals for reactive config values
  configGlobalSignal = this.globalConfig.configGlobal;
  
  // Create a signal for order to make it reactive
  orderSignal = signal<OrderResponse | undefined>(undefined);
  
  // FIXED: Computed signal for shipping cost
  shippingCost = computed(() => {
    const order = this.orderSignal();
    const config = this.configGlobalSignal();
    if (order && this.calculateTotal(order) >= config.seuilLivraisonGratuite) {
      return 0;
    }
    return config.valeurLivraison;
  });
  
  // Keep these as regular properties for backward compatibility
  get order() { return this.orderSignal(); }
  set order(value: OrderResponse | undefined) { this.orderSignal.set(value); }
  
  get shipping() {
   return this.shippingCost() }
  get seuil() { return this.configGlobalSignal().seuilLivraisonGratuite; }

  facture: FactureResponse | undefined;
  products: Product[] = [];
  isLoading = false;
  updateStatusLoading = false;
  errorMessage = '';
  currentDate = new Date();
  dueDate: Date | undefined;
  statut: string = '';
  addressForm!: FormGroup;
  customerDetailsForm!: FormGroup;
  orderItemForm!: FormGroup;
  showAddressForm = false;
  showCustomerDetailsForm = false;
  showOrderItemForm = false;
  addressFormType: 'LIVRAISON' | 'FACTURATION' = 'LIVRAISON';
  editingAddress: AdresseResponse | null = null;
  editingCustomerDetails: CustomerForm | null = null;
  editingOrderItem: any = null;
  countries = environment.countries.sort((a, b) => a.name.localeCompare(b.name));
  defaultCountry = CountryISO.Tunisia;
  preferredCountries = [CountryISO.France, CountryISO.Tunisia];
  phoneNumberFormat = PhoneNumberFormat.INTERNATIONAL;
  availableProducts: Product[] = [];
  selectedProduct: Product | null = null;
  availableColors: string[] = [];
  availableSizes: string[] = [];
  availableStock: number = 0;
  selectedVariant: variantResponse | null = null;
  productsLoading = false;
  @ViewChild('invoiceTemplate', { read: ElementRef }) invoiceElement!: ElementRef;

  constructor(
    private orderService: OrderService,
    private route: ActivatedRoute,
    private factureService: FactureService,
    private fb: FormBuilder,
    private adresseService: AdresseService,
    private productService: ProductService,
    private dialog: MatDialog,
    private router: Router
  ) {
    this.addressForm = this.fb.group({
      ligne1: ['', Validators.required],
      ligne2: [''],
      ville: ['', Validators.required],
      codePostal: ['', Validators.required],
      pays: ['', Validators.required]
    });
    
    this.customerDetailsForm = this.fb.group({
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required]],
    });
    
    this.orderItemForm = this.fb.group({
      productId: [null, Validators.required],
      couleur: ['', Validators.required],
      taille: [{ value: '', disabled: true }, Validators.required],
      quantite: [{ value: 1, disabled: true }, [Validators.required, Validators.min(1)]],
      prix: ['', Validators.required],
      variantId: [''],
    }, {
      validators: [this.validateMaxQuantity(), this.notSelectedVariantValidator()]
    });
    
    this.orderItemForm.get('variantId')?.valueChanges.subscribe(variantId => {
      const control = this.orderItemForm.get('quantite');
      if (variantId) {
        control?.enable();
      } else {
        control?.disable();
        control?.reset(1);
      }
    });
    
    this.orderItemForm.get('couleur')?.valueChanges.subscribe(couleur => {
      const control = this.orderItemForm.get('taille');
      if (couleur) {
        control?.enable();
      } else {
        control?.disable();
        control?.reset('');
      }
    });
  }

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.loadOrderDetails(+orderId);
    }
  }
  
  mergeInvoice() {
    // FIXED: Call signals when checking their value
    if (!this.isLoggedIn()) {
      const dialogRef = this.dialog.open(UserSigninComponent, {
        data: { mergeCart: false, mergeOrders: true },
        panelClass: 'custom-login-dialog'
      });
      dialogRef.afterClosed().subscribe({
        next: (isMerged) => {
          if (isMerged) {
            this.router.navigate(['/profile']);
          }
        },
        error: (error) => console.error(error)
      });
    }
  }
  
  downloadPdf() {
    if (!this.invoiceElement) {
      console.error('Invoice element not found');
      return;
    }
    if (!this.facture) {
      console.error('Facture not found');
      return;
    }
    this.pdfService.downloadPDF(this.invoiceElement, this.facture, 'facture');
  }

  loadOrderDetails(orderId: number): void {
    this.isLoading = true;
    this.orderService.getOrderById(orderId, this.authState().id).subscribe({
      next: (processedOrder) => {
        // FIXED: Update the signal
        this.order = processedOrder;
        this.statut = processedOrder.statut;
        // REMOVED: shipping calculation - now handled by computed signal
        
        this.loadFacture(orderId).subscribe({
          next: (facture) => {
            this.facture = facture;
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Error loading facture:', err);
            this.errorMessage = 'SWAL.ORDER_ERROR';
            this.isLoading = false;
            this.swalService.error(this.errorMessage);
          }
        });
      },
      error: (err) => {
        console.error('Error loading order:', err);
        this.errorMessage = 'SWAL.ORDER_ERROR';
        this.isLoading = false;
        this.swalService.error(this.errorMessage);
      }
    });
  }

  loadFacture(orderId: number): Observable<any> {
    return this.factureService.getFactureByOrderId(orderId);
  }

  loadAvailableProducts(): Observable<Product[]> {
    return this.productService.getAllProducts().pipe(
      tap((products) => {
        this.availableProducts = products;
      })
    );
  }

  onProductSelect(productId: string): void {
    const product = this.availableProducts.find(p => p.id === +productId);
    const productInOrder = this.order?.produits.find(item => item.productId === +productId);
    
    if (productInOrder && productInOrder.productId) {
      this.productService.getProductById(productInOrder.productId).subscribe(product => {
        this.selectedProduct = product;
        this.availableColors = [...new Set(product.variants.map(v => v.color))];
        this.availableSizes = [...new Set(product.variants.map(v => v.size))];
        this.selectedVariant = product.variants.find(v => v.color === productInOrder.couleur && v.size === productInOrder.taille) || null;
        
        const variantInOrder = this.order?.produits.find(item => item.variantId === this.selectedVariant?.id);
        this.availableStock = this.selectedVariant
          ? this.selectedVariant.quantity + (variantInOrder?.quantite || 0)
          : 0;

        this.orderItemForm.patchValue({
          quantite: productInOrder.quantite,
          prix: product.newPrice || product.prix
        });
      });
    } else if (product) {
      this.selectedProduct = product;
      this.availableColors = [...new Set(product.variants.map(v => v.color))];
      this.availableSizes = [];
      
      this.orderItemForm.patchValue({
        couleur: '',
        taille: '',
        prix: product.newPrice || product.prix
      });
      
      this.selectedVariant = null;
    }
  }

  onColorSelect(color: string): void {
    if (!this.selectedProduct) return;
    
    this.availableSizes = [...new Set(
      this.selectedProduct.variants
        .filter(v => v.color === color)
        .map(v => v.size)
    )];
    
    this.orderItemForm.patchValue({
      taille: ''
    });
    
    this.selectedVariant = null;
  }

  onSizeSelect(size: string): void {
    if (!this.selectedProduct) return;
    
    const color = this.orderItemForm.get('couleur')?.value;
    
    this.selectedVariant = this.selectedProduct.variants.find(
      v => v.color === color && v.size === size
    ) || null;
    
    if (this.selectedVariant) {
      this.orderItemForm.patchValue({
        variantId: this.selectedVariant.id,
      });
      
      const variantInOrder = this.order?.produits.find(item => item.variantId === this.selectedVariant?.id);
      this.availableStock = this.selectedVariant
        ? this.selectedVariant.quantity + (variantInOrder?.quantite || 0)
        : 0;

      const quantityControl = this.orderItemForm.get('quantite');
      quantityControl?.setValidators([
        Validators.required,
        Validators.min(1),
        Validators.max(this.availableStock)
      ]);
      quantityControl?.updateValueAndValidity();
    }
  }
  
  validateMaxQuantity(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const quantity = group.get('quantite')?.value;
      const maxQuantity = this.availableStock;
      return quantity > maxQuantity ? { maxQuantityExceeded: true } : null;
    };
  }
  
  notSelectedVariantValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const variantId = group.get('variantId')?.value;
      return variantId ? null : { notSelectedVariant: true };
    };
  }

  calculateTotal(order: OrderResponse | undefined): number {
    if (!order?.produits) return 0;
    return order.produits.reduce((total, product) => total + (product.prixTotal || 0), 0);
  }
  
  getStatusText(status: string): string {
    switch (status) {
      case 'LIVREE': return 'Delivered';
      case 'EN_COURS': return 'In Progress';
      case 'ANNULEE': return 'Annulled';
      case 'RETOUR': return 'Return';
      case 'EXPEDIEE': return 'Shipped';
      case 'CONFIRMEE': return 'Confirmed';
      case 'EN_COURS_PREPARATION': return 'In Progress';
      case 'TERMINEE': return 'Completed';
      case 'EN_ATTENTE': return 'Pending';
      default: return 'Pending';
    }
  }

  updateOrderStatus() {
    if (!this.order) return;
    this.updateStatusLoading = true;

    this.orderService.updateOrderStatus(this.order.id, { status: this.statut }).subscribe({
      next: () => {
        this.updateStatusLoading = false;
        this.loadOrderDetails(this.order!.id);
        this.swalService.success('SWAL.ORDER_STATUS_UPDATED');
      },
      error: (error) => {
        this.updateStatusLoading = false;
        console.error('Error updating order status:', error);
        this.swalService.error('SWAL.ORDER_STATUS_UPDATE_FAILED');
      }
    });
  }

  submitAddress(): void {
    if (this.addressForm.invalid) return;

    const payload = { ...this.addressForm.value };
    const req$ = this.editingAddress 
      ? this.adresseService.updateAdresse(this.editingAddress.id, payload) 
      : this.adresseService.createAdresse(payload);

    req$.subscribe({
      next: () => {
        this.loadOrderDetails(this.order!.id);
        this.closeAddressForm();
        this.swalService.success(this.editingAddress ? 'SWAL.ADDRESS.SUCCESS_UPDATE' : 'SWAL.ADDRESS.SUCCESS_CREATE');
      },
      error: () => this.swalService.error(this.editingAddress ? 'SWAL.ADDRESS.FAILED_UPDATE' : 'SWAL.ADDRESS.FAILED_CREATE')
    });
  }

  submitCustomerDetails(): void {
    if (this.customerDetailsForm.invalid) return;
    const customerDetails = this.customerDetailsForm.value;
    this.orderService.updateCustomerDetails(this.order!.id, customerDetails).subscribe({
      next: () => {
        this.loadOrderDetails(this.order!.id);
        this.closeCustomerDetailsForm();
        this.swalService.success('SWAL.CUSTOMER_DETAILS.SUCCESS_UPDATE');
      },
      error: () => this.swalService.error('SWAL.CUSTOMER_DETAILS.FAILED_UPDATE')
    });
  }

  submitOrderItem(): void {
    if (this.orderItemForm.invalid || !this.order || !this.selectedVariant) return;

    const formValue = this.orderItemForm.getRawValue();
    const prixTotal = formValue.prix * formValue.quantite;    

    let shipping = this.shipping;
    if (this.editingOrderItem)
      {this.order.produits = this.order.produits.map(item => item.variantId === this.editingOrderItem.variantId ? { ...item, quantite: formValue.quantite, prixTotal } : item);

  }
    else{
      this.order.produits.push({ variantId: this.selectedVariant.id!, quantite: formValue.quantite, prixTotal });      
    }
    
    const total = this.calculateTotal(this.order);        
    if(total >= this.seuil)
      shipping = 0;
    else
      shipping = this.configGlobalSignal().valeurLivraison;  
    const orderItem = {
      variantId: this.selectedVariant.id,
      quantite: formValue.quantite,
      prixTotal: prixTotal,
      shipping: shipping
    };

    this.orderService.updateOrderItem(this.order.id, orderItem).subscribe({
      next: () => {
        this.loadOrderDetails(this.order!.id);
        this.closeOrderItemForm();
        this.swalService.success('SWAL.ORDER_ITEM_SUCCESS_UPDATE');
      },
      error: () => this.swalService.error('SWAL.ORDER_ITEM_FAILED_UPDATE')
    });
  }

  removeOrderItem(variantId: number): void {
    if (!this.order) return;
    
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Item',
        message: 'Are you sure you want to delete this item?'
      }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.orderService.deleteOrderItem(this.order!.id, variantId).subscribe({
          next: () => {
            
            this.loadOrderDetails(this.order!.id);
            this.swalService.success('SWAL.ORDER_ITEM_DELETED');
          },
          error: () => this.swalService.error('SWAL.ORDER_ITEM_DELETED_FAILED')
        });
      }
    });
  }

  openAddressForm(type: 'LIVRAISON' | 'FACTURATION', address?: AdresseResponse): void {
    this.addressFormType = type;
    this.editingAddress = address || null;
    this.showAddressForm = true;
    address ? this.addressForm.patchValue(address) : this.addressForm.reset();
  }

  openCustomerForm(customer?: CustomerForm): void {
    this.editingCustomerDetails = customer || null;
    this.showCustomerDetailsForm = true;
    customer ? this.customerDetailsForm.patchValue(customer) : this.customerDetailsForm.reset();
  }

  openOrderItemForm(item?: any): void {
    this.editingOrderItem = item || null;
    this.showOrderItemForm = true;
    this.selectedProduct = null;
    this.productsLoading = true;
    
    if (item) {
      this.loadAvailableProducts().subscribe({
        next: (products) => {
          this.availableProducts = products.filter(p => this.order?.produits.some(orderItem => orderItem.productId === p.id));
          
          this.selectedProduct = this.availableProducts.find(p => p.id === item.productId) || null;
          if (this.selectedProduct) {
            this.availableColors = [...new Set(this.selectedProduct.variants.map(v => v.color))];
            this.availableSizes = [...new Set(
              this.selectedProduct.variants
                .filter(v => v.color === item.couleur)
                .map(v => v.size)
            )];
            this.selectedVariant = this.selectedProduct.variants.find(
              v => v.color === item.couleur && v.size === item.taille
            ) || null;

            const variantInOrder = this.order?.produits.find(orderItem => orderItem.variantId === this.selectedVariant?.id);
            this.availableStock = this.selectedVariant
              ? this.selectedVariant.quantity + (variantInOrder?.quantite || 0)
              : 0;
          }
          
          this.orderItemForm.patchValue({
            productId: item.productId,
            couleur: item.couleur,
            taille: item.taille,
            quantite: item.quantite,
            prix: this.selectedProduct!.newPrice || this.selectedProduct!.prix || item.prix,
            variantId: this.selectedVariant?.id || ''
          });
          this.productsLoading = false;
        },
        error: () => {
          this.productsLoading = false;
          this.swalService.error('SWAL.PRODUCTS.LOAD_FAILED');
        }
      });
    } else {
      this.orderItemForm.reset({ quantite: 1 });
      this.selectedProduct = null;
      this.selectedVariant = null;
      this.availableColors = [];
      this.availableSizes = [];
      this.productsLoading = false;
      
      this.loadAvailableProducts().subscribe({
        next: (products) => {
          this.availableProducts = products.filter(p => !this.order?.produits.some(item => item.productId === p.id));
        },
        error: () => {
          this.productsLoading = false;
          this.swalService.error('SWAL.PRODUCTS.LOAD_FAILED');
        }
      });
    }
  }

  closeAddressForm(): void {
    this.showAddressForm = false;
    this.editingAddress = null;
    this.addressForm.reset();
  }

  closeCustomerDetailsForm(): void {
    this.showCustomerDetailsForm = false;
    this.editingCustomerDetails = null;
    this.customerDetailsForm.reset();
  }

  closeOrderItemForm(): void {
    this.showOrderItemForm = false;
    this.editingOrderItem = null;
    this.selectedProduct = null;
    this.selectedVariant = null;
    this.availableColors = [];
    this.availableSizes = [];
    this.orderItemForm.reset();
  }

  allowOnlyNumbers(event: KeyboardEvent): void {
    const allowed = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab'];
    if (!/\d/.test(event.key) && !allowed.includes(event.key)) event.preventDefault();
  }
}