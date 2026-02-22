import { Component, computed } from '@angular/core';
import { ThemeService, Theme } from '../../../core/services/theme.service';

@Component({
  selector: 'app-theme-selector',
  standalone: true,
  templateUrl: './theme-selector.component.html'
})
export class ThemeSelectorComponent {
  currentTheme = computed(() => this.themeService.currentTheme$());

  constructor(private themeService: ThemeService) {
  }

  onThemeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.themeService.setTheme(select.value as Theme);
  }
}