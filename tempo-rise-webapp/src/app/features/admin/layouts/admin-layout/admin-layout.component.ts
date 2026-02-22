import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MainHeaderComponent } from '../../../../shared/components/main-header/main-header.component';
import { ThemeSelectorComponent } from '../../theme-selector/theme-selector.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { AdminSidebarComponent } from '../../admin-sidebar/admin-sidebar.component';
import { CartComponent } from '../../../user/cart/cart.component';
import { CommonModule } from '@angular/common';
import { SocialLinksComponent } from "../../../../shared/components/social-links/social-links.component";

@Component({
    selector: 'app-admin-layout',
    imports: [RouterOutlet, MainHeaderComponent,
    ThemeSelectorComponent, FooterComponent,
    AdminSidebarComponent, CartComponent, CommonModule, SocialLinksComponent],
    templateUrl: './admin-layout.component.html'
})
export class AdminLayoutComponent {
    sidebarOpen = true;
    toggleSidebar() {
        this.sidebarOpen = !this.sidebarOpen;
      }
}