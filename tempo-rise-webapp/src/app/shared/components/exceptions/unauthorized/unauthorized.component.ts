import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  imports: [RouterLink],
  template: `
  <div class="min-h-screen flex items-center justify-center bg-[var(--background)]">
    <div class="text-center">
      <h1 class="text-6xl font-bold text-[var(--primary)] mb-4">401</h1>
      <h2 class="text-2xl font-semibold text-[var(--text)] mb-6">Accès non autorisé</h2>
      <p class="text-[var(--text)] mb-8">
        Vous n'avez pas les permissions nécessaires pour accéder à cette page.
      </p>
      <a 
        routerLink="/" 
        class="px-6 py-3 bg-[var(--primary)] text-white rounded-lg hover:opacity-90 transition"
      >
        Retour à l'accueil
      </a>
    </div>
  </div>
`,
  styleUrl: './unauthorized.component.css'
})
export class UnauthorizedComponent {

}
