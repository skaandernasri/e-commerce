import { computed, effect, inject, Pipe, PipeTransform } from '@angular/core';
import { LanguageService } from '../services/language.service';

@Pipe({
  name: 'currencyFormat',
  pure: false
})
export class CurrencyFormatPipe implements PipeTransform {
  private languageService = inject(LanguageService);
  private currencyConfig: Record<string, { code: string; symbol: string; locale: string, direction: string }> = {
    'ar': { code: 'TND', symbol: 'د.ت', locale: 'ar-TN', direction: 'rtl' },
    'fr': { code: 'EUR', symbol: 'TND', locale: 'fr-FR', direction: 'rtl' },
    'en': { code: 'USD', symbol: 'TND', locale: 'en-US', direction: 'rtl' }
  };
  private config = computed(() => {
    const lang = this.languageService.language();
    return this.currencyConfig[lang] || this.currencyConfig['fr'];
  });

transform(value: number): string {
  const config = this.config();

  // Use 2 decimal places (standard for currencies)
  const decimals = 2;
  
  if (config.direction === 'rtl') {
    if(value)
      return `${value.toFixed(decimals)} ${config.symbol}`;
    else
      return `0.00 ${config.symbol} `;
  }

  return new Intl.NumberFormat(config.locale, {
    style: 'currency',
    currency: config.code,
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(value);
}

}