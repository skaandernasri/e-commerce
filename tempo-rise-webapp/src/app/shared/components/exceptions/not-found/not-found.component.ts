// not-found.component.ts
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MainHeaderComponent } from "../../main-header/main-header.component";
import { FooterComponent } from "../../footer/footer.component";

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, MainHeaderComponent, FooterComponent],
template: `
  <app-main-header class="z-10 transition-all duration-300 "></app-main-header>

  <div class="min-h-screen flex items-center justify-center bg-[var(--background)] overflow-auto">
    <div class="text-center">
      <h1 class="text-6xl font-bold text-[var(--primary)] mb-4">404</h1>
      <h2 class="text-2xl font-semibold text-[var(--text)] mb-6">Page non trouvée</h2>
      <p class="text-[var(--text)] mb-8">
        La page que vous recherchez n'existe pas ou a été déplacée.
      </p>
      <a 
        routerLink="/"
        (click)="navigatePrecedentPage()"
        class="px-6 py-3 bg-[var(--primary)] text-white rounded-lg hover:opacity-90 transition"
      >
        Retour à la page précedente
      </a>
    </div>
  </div>
  <app-footer></app-footer>

`,

  styles: []
})
export class NotFoundComponent {
  navigatePrecedentPage() {
    window.history.back();
  }

}