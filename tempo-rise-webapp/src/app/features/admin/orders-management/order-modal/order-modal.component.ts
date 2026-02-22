import { Component, inject, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { ProductService } from '../../../../core/services/product.service';
import { Product, variantResponse } from '../../../../core/models/product';
import { AdresseService } from '../../../../core/services/adresse.service';
import { UsersService } from '../../../../core/services/users.service';
import { CommonModule } from '@angular/common';
import { CurrencyFormatPipe } from '../../../../core/pipe/currency-pipe.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { SwalService } from '../../../../core/services/swal-service.service';
import { OrderService } from '../../../../core/services/order.service';
import { CountryISO, NgxMaterialIntlTelInputComponent } from 'ngx-material-intl-tel-input';
import { environment } from '../../../../../environments/environment';
import { ConfigGlobalService } from '../../../../core/services/config-global.service';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-order-modal',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    CurrencyFormatPipe, 
    TranslateModule, 
    NgxMaterialIntlTelInputComponent,
    MatFormFieldModule,
    MatInputModule
  ],
  providers: [provideAnimations()],
  templateUrl: './order-modal.component.html'
})
export class OrderModalComponent implements OnInit {
  private swalService = inject(SwalService);
  private configGlobalService = inject(ConfigGlobalService);
  
  products = signal<Product[]>([]);
  adressLivraison = signal<any[]>([]);
  adressFacturation = signal<any[]>([]);
  users = signal<any[]>([]);
  
  orderForm: FormGroup;
  isLoading = false;
  isSubmitting = false;
  
  // Product selection state - track per product item
  selectedProducts = new Map<number, Product>();
  availableColorsMap = new Map<number, string[]>();
  availableSizesMap = new Map<number, string[]>();
  selectedVariantsMap = new Map<number, variantResponse>();
  
  // User address management
  userAddresses = signal<any[]>([]);
  showNewAddressForm = false;
  addressFormType: 'LIVRAISON' | 'FACTURATION' = 'LIVRAISON';
  newAddressForm: FormGroup;
  
  // Phone input config
  defaultCountry = CountryISO.Tunisia;
  preferredCountries = [CountryISO.France, CountryISO.Tunisia];
  countries = environment.countries.sort((a, b) => a.name.localeCompare(b.name));
  
  imagesBaseUrl = environment.imageUrlBase;

  constructor(
    public dialogRef: MatDialogRef<OrderModalComponent>,
    private fb: FormBuilder,
    private productService: ProductService,
    private adressService: AdresseService,
    private userService: UsersService,
    private orderService: OrderService
  ) {
    this.orderForm = this.fb.group({
      utilisateurId: [null, Validators.required],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', Validators.required],
      modePaiement: ['A_LIVRAISON', Validators.required],
      codePromo: [null],
      adresseLivraisonId: [null, Validators.required],
      adresseFacturationId: [null, Validators.required],
      produits: this.fb.array([], [Validators.required, Validators.minLength(1)]),
    });

    this.newAddressForm = this.fb.group({
      ligne1: ['', Validators.required],
      ligne2: [''],
      ville: ['', Validators.required],
      codePostal: ['', Validators.required],
      pays: ['', Validators.required],
      type: ['LIVRAISON', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    
    // Load products
    this.productService.getAllProducts().subscribe({
      next: (res) => {
        this.products.set(res.filter(p => p.actif));
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.swalService.error('Failed to load products');
      }
    });

    // Load users
    this.userService.getAllUsers().subscribe({
      next: (res) => {
        this.users.set(res);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.swalService.error('Failed to load users');
        this.isLoading = false;
      }
    });
  }

  get produitsArray(): FormArray {
    return this.orderForm.get('produits') as FormArray;
  }

  onUserSelect(userId: number) {
    const user = this.users().find(u => u.id === userId);
    if (user) {
      this.orderForm.patchValue({
        nom: user.nom,
        prenom: user.prenom,
        email: user.email,
        telephone: user.telephone
      });
      
      // Load user addresses
      this.loadUserAddresses(userId);
    }
  }

  loadUserAddresses(userId: number) {
    this.adressService.getAdresseByUserId(userId).subscribe({
      next: (addresses) => {
        this.userAddresses.set(addresses);
        this.adressLivraison.set(addresses.filter(a => a.type === 'LIVRAISON'));
        this.adressFacturation.set(addresses.filter(a => a.type === 'FACTURATION'));
      },
      error: (err) => {
        console.error('Error loading user addresses:', err);
        this.swalService.error('Failed to load user addresses');
      }
    });
  }

  // Product item management
  createProductItem(): FormGroup {
    return this.fb.group({
      productId: ['', Validators.required],
      variantId: ['', Validators.required],
      nom: ['', Validators.required],
      couleur: ['', Validators.required],
      taille: ['', Validators.required],
      quantite: [1, [Validators.required, Validators.min(1)]],
      prix: [0, Validators.required],
      prixTotal: [0, Validators.required],
      newPrice: [0, Validators.required],
      imageProduits: [[]]
    });
  }

  addProductItem() {
    this.produitsArray.push(this.createProductItem());
  }

  removeProductItem(index: number) {
    this.produitsArray.removeAt(index);
    // Clean up tracking maps
    this.selectedProducts.delete(index);
    this.availableColorsMap.delete(index);
    this.availableSizesMap.delete(index);
    this.selectedVariantsMap.delete(index);
    this.calculateTotal();
  }

  onProductSelect(index: number, productId: string) {
    const product = this.products().find(p => p.id === +productId);
    if (!product) return;
    // Store product for this index
    this.selectedProducts.set(index, product);
    const productGroup = this.produitsArray.at(index) as FormGroup;
    
    // Extract unique colors for this product
    const colors = [...new Set(product.variants.map(v => v.color))];
    this.availableColorsMap.set(index, colors);
    this.availableSizesMap.set(index, []);
    
    productGroup.patchValue({
      productId: product.id,
      nom: product.nom,
      prix: product.prix,
      imageProduits: product.imageProduits,
      couleur: '',
      taille: '',
      variantId: '',
      newPrice: product.newPrice || product.prix,
      prixTotal: 0
    });
  }

  onColorSelect(index: number, color: string) {
    const product = this.selectedProducts.get(index);
    if (!product) return;
    
    const productGroup = this.produitsArray.at(index) as FormGroup;
    
    // Filter sizes for selected color
    const sizes = [...new Set(
      product.variants
        .filter(v => v.color === color)
        .map(v => v.size)
    )];
    this.availableSizesMap.set(index, sizes);
    
    productGroup.patchValue({
      couleur: color,
      taille: '',
      variantId: '',
      prixTotal: 0
    });
  }

  onSizeSelect(index: number, size: string) {
    const product = this.selectedProducts.get(index);
    if (!product) return;
    
    const productGroup = this.produitsArray.at(index) as FormGroup;
    const color = productGroup.get('couleur')?.value;
    
    if (!color) return;
    
    // Find variant
    const variant = product.variants.find(
      v => v.color === color && v.size === size
    );
    
    if (variant) {
      this.selectedVariantsMap.set(index, variant);
      productGroup.patchValue({
        taille: size,
        variantId: variant.id
      });
      
      this.updateProductTotal(index);
    }
  }

  onQuantityChange(index: number) {
    this.updateProductTotal(index);
  }

  updateProductTotal(index: number) {
    const productGroup = this.produitsArray.at(index) as FormGroup;
    const prix = productGroup.get('newPrice')?.value || 0;
    const quantite = productGroup.get('quantite')?.value || 0;
    const prixTotal = prix * quantite;
    
    productGroup.patchValue({
      prixTotal: prixTotal
    }, { emitEvent: false });
    
    this.calculateTotal();
  }

  calculateSubtotal(): number {
    let subtotal = 0;
    this.produitsArray.controls.forEach(control => {
      const prixTotal = control.get('prixTotal')?.value;
      subtotal += prixTotal ? Number(prixTotal) : 0;
    });
    return subtotal;
  }

  calculateShipping(): number {
    const subtotal = this.calculateSubtotal();
    const config = this.configGlobalService.configGlobal();
    
    if (subtotal >= config.seuilLivraisonGratuite) {
      return 0;
    }
    return config.valeurLivraison;
  }

  calculateTotal(): number {
    return this.calculateSubtotal() + this.calculateShipping();
  }

  // Helper method to get available colors for a specific product item
  getAvailableColors(index: number): string[] {
    return this.availableColorsMap.get(index) || [];
  }

  // Helper method to get available sizes for a specific product item
  getAvailableSizes(index: number): string[] {
    return this.availableSizesMap.get(index) || [];
  }

  // Address management
  openNewAddressForm(type: 'LIVRAISON' | 'FACTURATION') {
    this.addressFormType = type;
    this.showNewAddressForm = true;
    this.newAddressForm.patchValue({ type });
  }

  closeNewAddressForm() {
    this.showNewAddressForm = false;
    this.newAddressForm.reset({ type: 'LIVRAISON' });
  }

  submitNewAddress() {
    if (this.newAddressForm.invalid) {
      this.swalService.error('Please fill all required address fields');
      Object.keys(this.newAddressForm.controls).forEach(key => {
        this.newAddressForm.get(key)?.markAsTouched();
      });
      return;
    }
    
    const userId = this.orderForm.get('utilisateurId')?.value;
    if (!userId) {
      this.swalService.error('Please select a user first');
      return;
    }

    const addressData = {
      ...this.newAddressForm.value,
      utilisateurId: userId
    };

    this.adressService.createAdresse(addressData).subscribe({
      next: (newAddress) => {
        this.swalService.success('Address added successfully');
        this.loadUserAddresses(userId);
        
        // Auto-select the new address
        if (this.addressFormType === 'LIVRAISON') {
          this.orderForm.patchValue({ adresseLivraisonId: newAddress.id });
        } else {
          this.orderForm.patchValue({ adresseFacturationId: newAddress.id });
        }
        
        this.closeNewAddressForm();
      },
      error: (err) => {
        console.error('Error creating address:', err);
        this.swalService.error('Failed to add address');
      }
    });
  }

  // Form submission
  onSubmit() {
    // Mark all fields as touched to show validation errors
    Object.keys(this.orderForm.controls).forEach(key => {
      this.orderForm.get(key)?.markAsTouched();
    });

    // Mark all product items as touched
    this.produitsArray.controls.forEach((group) => {
      Object.keys((group as FormGroup).controls).forEach(key => {
        group.get(key)?.markAsTouched();
      });
    });

    if (this.orderForm.invalid) {
      this.swalService.error('Please fill all required fields');
      return;
    }

    if (this.produitsArray.length === 0) {
      this.swalService.error('Please add at least one product');
      return;
    }

    this.isSubmitting = true;

    const orderData = {
      ...this.orderForm.value,
      total: this.calculateSubtotal(),
      shipping: this.calculateShipping(),
      token: 'TND',
    };

    this.orderService.adminCreateOrder(orderData).subscribe({
      next: () => {
        this.swalService.success('Order created successfully');
        this.dialogRef.close(true);
      },
      error: (err) => {
        console.error('Error creating order:', err);
        this.swalService.error(err?.error?.message || 'Failed to create order');
        this.isSubmitting = false;
      }
    });
  }

  onCancel() {
    this.dialogRef.close(false);
  }

  allowOnlyNumbers(event: KeyboardEvent): void {
    const allowed = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete', 'Home', 'End'];
    if (!/\d/.test(event.key) && !allowed.includes(event.key)) {
      event.preventDefault();
    }
  }
}