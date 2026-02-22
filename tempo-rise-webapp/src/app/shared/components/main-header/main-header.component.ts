import { Component, computed, effect, ElementRef, HostListener, inject, OnDestroy, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CartService } from '../../../core/services/cart.service';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language.service';
import { AuthService } from '../../../core/services/auth.service';
import { Subject } from 'rxjs';
import { ProductService } from '../../../core/services/product.service';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import { Product } from '../../../core/models/product';
import { FormsModule } from '@angular/forms';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { NotifService } from '../../../core/services/notif.service';
import { ThemeService } from '../../../core/services/theme.service';
import { environment } from '../../../../environments/environment';
import { NotifNumberPipe } from '../../../core/pipe/notif-number.pipe';
import { BannerSectionComponent } from "../../../features/admin/param-management/All-page-sections/banner-section/banner-section.component";
import { BannerComponent } from "../banner/banner.component";
import { ConfigGlobalService } from '../../../core/services/config-global.service';

interface NavLink {
  path: string;
  translationKey: string;
  exact: boolean;
}

@Component({
  selector: 'app-main-header',
  standalone: true,
  imports: [CommonModule,
    RouterLink,
    RouterLinkActive,
    TranslateModule,
    MatAutocompleteModule, FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    CurrencyFormatPipe,
    NotifNumberPipe, BannerComponent],
  templateUrl: './main-header.component.html',
})
export class MainHeaderComponent implements  OnDestroy, OnInit {
  // Component state
  authService = inject(AuthService);
  cartService = inject(CartService);
  productService = inject(ProductService);
  notifService = inject(NotifService);
  globalConfig = inject(ConfigGlobalService);
  products = this.productService.headerProducts;
  imageBaseUrl = environment.imageUrlBase;
  searchTerm = signal('');
  isUserMenuOpen = false;
  isMobileMenuOpen = false;
  isMobileSearchOpen = false;
  isLanguageMenuOpen = false;
  loadingMore = false;
  isLoggedIn = this.authService.isLoggedIn;
  isAdmin=computed(() => this.authState().roles.includes('ROLE_ADMIN') || this.authState().roles.includes('ROLE_SUPER_ADMIN')|| this.authState().roles.includes('ROLE_GESTIONNAIRE'));
  authState=this.authService.authState;
  // Constants
  readonly languages = ['en', 'fr', 'ar'] as const;
  readonly navLinks: NavLink[] = [
    { path: '/', translationKey: 'HEADER.HOME', exact: true },
    { path: '/products', translationKey: 'HEADER.PRODUCTS', exact: false },
    { path: '/blog', translationKey: 'HEADER.BLOG', exact: false },
    { path: '/contact', translationKey: 'HEADER.CONTACT', exact: false }
  ];
  
  @ViewChild('notifDropdown') notifDropdownRef!: ElementRef;
  @ViewChild('userDropdown') userDropdownRef!: ElementRef;
  @ViewChild('languageDropdown') languageDropdownRef!: ElementRef;
  @ViewChild('productsDropdown') productsDropdownRef!: ElementRef;
  @ViewChild('searchInput') searchInput!: ElementRef;
  private readonly destroy$ = new Subject<void>();

  constructor(
    //private readonly cartService: CartService,
    public readonly languageService: LanguageService,
    private router: Router,
    readonly themeService: ThemeService,
  ) {
    effect(() => {
      if(this.notifService.isNotifOpen())
        this.notifService.readAllNotifs(this.authService.authState().id!).subscribe();      
    });
    effect(() => {this.searchTerm(),
    this.loadProducts();
    }
  );
  }
  notifs=computed(() => this.authService.authState().isAuthenticated ? this.notifService.notifsArray():[]);
  hasMoreNotifs=computed(() => this.notifService.hasMoreNotifs());
  notifCount=computed(() => this.authService.authState().isAuthenticated ? this.notifService.unreadCount():0);
  cartItemCount=computed(() => this.authService.authState().isAuthenticated ? this.cartService.cartItemCount() : this.cartService.guestCartItemCount());
  showNotifications = computed(() => this.notifService.isNotifOpen());


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
ngOnInit(): void {
    this.globalConfig.getConfigGlobal().subscribe();
}


  loadProducts(ignoreSearchEmpty=false): void {
    if(this.searchTerm() == "" && !ignoreSearchEmpty){
      this.productService.clearHeaderProducts();
      return;
    } 
    const params={
      productName:this.searchTerm(),
      categoryNames:this.searchTerm(),
      actif:true
    }
    this.productService.loadHeaderProducts(params);
  }
  loadMoreNotifs(userId: number): void {
    this.notifService.loadMoreNotifs(userId);
  }
  // Public methods
  displayProductName(product: Product): string {
  return product?.nom || '';
}
onProductSelected(product: Product): void {
  this.router.navigate(['/product', product.id]);
  this.productService.clearHeaderProducts();
}
  toggleUserMenu(event: Event): void {
    //event.stopPropagation();
    this.isUserMenuOpen = !this.isUserMenuOpen;
    //this.isLanguageMenuOpen = false;
  }
  toggleNotification() {
    this.notifService.toggleNotification();
  }
     @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const clickedInsideNotif = this.notifDropdownRef?.nativeElement.contains(target);
    const clickedInsideUserMenu = this.userDropdownRef?.nativeElement.contains(target);
    const clickedInsideLanguageMenu = this.languageDropdownRef?.nativeElement.contains(target);
    const clickedInsideProductsMenu = this.productsDropdownRef?.nativeElement.contains(target);
    const clickedInsideSearchMenu = this.searchInput?.nativeElement.contains(target);
    if (!clickedInsideNotif) {
      this.notifService.isNotifOpen.set(false);
    }
    if(!clickedInsideUserMenu){
      this.isUserMenuOpen = false;
    }
    if(!clickedInsideLanguageMenu){
      this.isLanguageMenuOpen = false;
    }
    if(!clickedInsideProductsMenu){
      this.productService.clearHeaderProducts();
    }
    if(clickedInsideSearchMenu ){
      this.loadProducts(true);
    }
  }
  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
toggleMobileSearch(): void {
  this.isMobileSearchOpen = !this.isMobileSearchOpen;
}
  toggleLanguageMenu(event: Event): void {
    //event.stopPropagation();
    this.isLanguageMenuOpen = !this.isLanguageMenuOpen;
    //this.isUserMenuOpen = false;
  }

  changeLanguage(lang: string): void {
    this.languageService.changeLanguage(lang);
    this.isLanguageMenuOpen = false;
  }

  toggleCart(): void {
    this.cartService.toggleCart();
  }

  logout(): void {
    this.authService.logout().subscribe();
  }









}