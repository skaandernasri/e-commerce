import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-admin-sidebar',
  templateUrl: './admin-sidebar.component.html',
  styleUrls: ['./admin-sidebar.component.css'],
  imports: [RouterLink,CommonModule,MatIconModule,RouterLinkActive,TranslateModule],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(-100%)' }),
        animate('200ms ease-in', style({ transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(-100%)' }))
      ])
    ]),
    trigger('expandAnimation', [
      transition(':enter', [
        style({ height: '0', opacity: 0, overflow: 'hidden' }),
        animate('200ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ height: '0', opacity: 0, overflow: 'hidden' }))
      ])
    ])
  ],
  
})
export class AdminSidebarComponent {
  @Input() isOpen = true;
  @Output() isOpenChange = new EventEmitter<boolean>(); // Must be [propName]Change

  menuItems = [
    {
    title: 'Paramétrage',
    icon: 'settings',
    expanded: false,
    //link: '/admin/params',
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_GENERAL', link: '/admin/params', icon: 'settings' },
      { title: 'ADMIN_SIDEBAR.MANAGE_SECTIONS', link: '/admin/params/sections', icon: 'settings' }
      
    ]
  },
  {
    title: 'ADMIN_SIDEBAR.PRODUCTS',
    icon: 'shopping_cart',
    expanded: false,
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_PRODUCTS', link: '/admin/products', icon: 'inventory' }
    ]
  },
  {
    title: 'ADMIN_SIDEBAR.USERS',
    icon: 'people',
    expanded: false,
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_USERS', link: '/admin/users', icon: 'manage_accounts' }
    ]
  },
  { title: 'ADMIN_SIDEBAR.ORDERS', icon: 'list_alt', link: '/admin/orders', subItems: [] },
  {
    title: 'ADMIN_SIDEBAR.INVOICES',
    icon: 'receipt_long',
    expanded: false,
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_INVOICES', link: '/admin/invoices', icon: 'receipt' }
    ]
  },
  { title: 'ADMIN_SIDEBAR.REVIEWS', icon: 'reviews', link: '/admin/reviews', subItems: [] },
  {
    title: 'ADMIN_SIDEBAR.BLOGS',
    icon: 'local_offer',
    expanded: false,
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_BLOGS', link: '/admin/blog', icon: 'local_fire_department' }
    ]
  },
  {
    title: 'ADMIN_SIDEBAR.CATEGORIES',
    icon: 'category',
    expanded: false,
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_CATEGORIES', link: '/admin/categories', icon: 'category' }
    ]
  },
  {
    title: 'ADMIN_SIDEBAR.PROMOTIONS',
    icon: 'local_offer',
    expanded: false,
    subItems: [
      { title: 'ADMIN_SIDEBAR.MANAGE_PROMOTIONS', link: '/admin/promotions', icon: 'local_fire_department' },
      { title: 'ADMIN_SIDEBAR.MANAGE_PROMO_CODES', link: '/admin/codesPromo', icon: 'confirmation_number' }
    ]
  },
  { title: 'ADMIN_SIDEBAR.CONTACTS', icon: 'mail', link: '/admin/contacts', subItems: [] },
  { title: 'ADMIN_SIDEBAR.RECOMMENDATION', icon: 'model_training', link: '/admin/recommandation-system', subItems: [] }
];

  
  constructor(private router:Router){}
  isLinkActive(url: string): boolean {
    return this.router.url.includes(url);
  }
  isParentActive(item: any): boolean {
    return item.subItems.some((subItem: any) => this.router.url.includes(subItem.link))||
    (item.link && this.router.url.includes(item.link));
  }
  navigateTo(link: string): void {
    this.router.navigate([link]);

  }
  toggleItem(item: any): void {
    item.expanded = !item.expanded;
  }
  toggleSidebar() {
    this.isOpen = !this.isOpen;
    this.isOpenChange.emit(this.isOpen);
  }
}