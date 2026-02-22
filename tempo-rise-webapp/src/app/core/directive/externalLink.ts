import { Directive, HostListener, Input } from '@angular/core';
import { Router } from '@angular/router';

@Directive({
  selector: '[appExternalLink]'
})
export class ExternalLinkDirective {
  @Input('href') href: string = '';

  constructor(private router: Router) {}

  @HostListener('click', ['$event'])
  handleClick(event: Event) {
    if (!this.href) return;

    const url = this.href;
    const isExternal = !url.startsWith(window.location.origin);

      if (isExternal) {
      event.preventDefault(); // Stop default navigation

      const confirmRedirect = window.confirm('You are about to leave this site. Do you want to continue?');
      if (confirmRedirect) {
        // Open external link in a new tab
        window.open(url, '_blank');
      }
      // Else: do nothing, user stays on the page
    }
  }
}
