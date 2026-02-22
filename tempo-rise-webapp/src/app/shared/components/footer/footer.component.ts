import { Component, computed, effect, inject, OnInit, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ParamsService } from '../../../core/services/params.service';
import { environment } from '../../../../environments/environment';
import { ExternalLinkDirective } from "../../../core/directive/externalLink";
import { FooterGroup, SocialLink } from '../../../core/models/params';
import { RouterLinkWithHref } from "@angular/router";

// New type to represent both items and groups

interface FooterContent {
  logoUrl: string;
  socials: SocialLink[];
  groups: FooterGroup[];
  copyright: string;
}
@Component({
    selector: 'app-footer',
    imports: [CommonModule, TranslateModule, ExternalLinkDirective, RouterLinkWithHref],
    templateUrl: './footer.component.html'
})
export class FooterComponent implements OnInit {
  private paramService = inject(ParamsService);
  currentYear = new Date().getFullYear();
  imagesBaseUrl = environment.imageUrlBase;
  activeFooterSection = computed(() => this.paramService.allPageSections().find(section => section.type === 'FOOTER' && section.active));

  footerContent:Signal<FooterContent | null> =computed(() => {
    const content = this.activeFooterSection()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        logoUrl: parsed.logoUrl ,
        socials: parsed.socials,
        groups: parsed.groups,
        copyright: parsed.copyright,
      };
    }
    return null;
  })
  
  // Separate computed for groups and items

  
  facebookUrl = 'https://www.facebook.com/profile.php?id=61581704426158';
  facebookLogoAlt = 'assets/images/home/Facebook Circled.png';
  instagramUrl = 'https://www.instagram.com/invites/contact/?utm_source=ig_contact_invite&utm_medium=copy_link&utm_content=zq9vqdn';
  instagramLogoAlt = 'assets/images/home/Instagram Circle.png';
  linkedinUrl = 'https://www.linkedin.com';
  linkedinLogoAlt = 'assets/images/home/mdi_linkedin.png';
  XUrl = 'https://twitter.com/temposphere';
  XLogoAlt = 'assets/images/home/X.png';
  tiktokUrl = 'https://www.tiktok.com/@temposphere.tn?_r=1&_t=ZM-911oeclWjk4';
  logoUrlAlt = 'assets/images/home/logo blanc sale - 1 1.png';

  getSocialLogoAlt(platform: string): string {
    if (platform.toLowerCase().includes('facebook')) {
      return this.facebookLogoAlt;
    } else if (platform.toLowerCase().includes('instagram')) {
      return this.instagramLogoAlt;
    }
    else if (platform.toLowerCase().includes('linkedin')) {
      return this.linkedinLogoAlt;
    }
    else if (platform.toLowerCase().includes('x')) {
      return this.XLogoAlt;
    }
    return '';
  }
  ngOnInit() {
    this.loadFooterSection();
  }

  loadFooterSection() {
    this.paramService.getSectionsByType('FOOTER').subscribe();
  }


}