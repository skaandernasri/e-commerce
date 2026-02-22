import { Injectable, signal } from '@angular/core';
import Swal, { SweetAlertOptions } from 'sweetalert2';
import { TranslateService } from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class SwalService {
  private messageKey=signal<string>('');
  private titleKey=signal<string>('');
  constructor(private translate: TranslateService) {
    // Subscribe to language changes
    this.translate.onLangChange.subscribe(() => {
      // If a popup is currently open, update its text
      if (Swal.isVisible()) {
        this.updateCurrentPopupLanguage();
      }
    });
  }

  /** Update current open Swal text when language changes */
  private updateCurrentPopupLanguage() {
    const popup = Swal.getPopup();
    if (!popup) return;

    // Identify text elements
    const titleEl = popup.querySelector('.swal2-title');
    const htmlEl = popup.querySelector('.swal2-html-container');
    const confirmBtn = popup.querySelector('.swal2-confirm');
    const cancelBtn = popup.querySelector('.swal2-cancel');

    // Get translated text again
    const currentTitle = titleEl?.textContent?.trim() || '';
    const currentText = htmlEl?.textContent?.trim() || '';

    this.translate.get(this.titleKey()).subscribe(newTitle => {
      if (titleEl) titleEl.textContent = newTitle;      
    });
    this.translate.get(this.messageKey()).subscribe(newText => {
      if (htmlEl) htmlEl.textContent = newText;      
    });

    if (confirmBtn) {
      this.translate.get('SWAL.ACTION_CONFIRM').subscribe(val => (confirmBtn.textContent = val));
    }
    if (cancelBtn) {
      this.translate.get('SWAL.CANCEL').subscribe(val => (cancelBtn.textContent = val));
    }
  }

  /** Base toast config */
  private baseToastConfig(): SweetAlertOptions {
    return {
      toast: true,
      position: 'top-start',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
      didOpen: (toast: HTMLElement) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
        toast.style.backgroundColor = 'var(--surface)';
        toast.style.color = 'var(--text)';
        toast.style.border = '1px solid var(--primary)';
        toast.style.borderRadius = '0.75rem';

        const icon = toast.querySelector<HTMLElement>('.swal2-icon');
        if (icon) {
          icon.style.borderColor = 'var(--primary)';
          icon.style.color = 'var(--primary)';
          icon.style.backgroundColor = 'transparent';
        }

        const title = toast.querySelector<HTMLElement>('.swal2-title');
        if (title) title.style.color = 'var(--text)';
      }
    };
  }

  private toast(type: 'success' | 'error' | 'warning' | 'info', messageKey: string) {
    this.messageKey.set(messageKey);
    this.titleKey.set(`SWAL.${type.toUpperCase()}`);
    this.translate.get([`SWAL.${type.toUpperCase()}`, messageKey]).subscribe(translations => {
      Swal.fire({
        ...this.baseToastConfig(),
        icon: type,
        title: translations[`SWAL.${type.toUpperCase()}`],
        text: translations[messageKey]
      });
    });
  }

  success(messageKey: string) { this.toast('success', messageKey); }
  error(messageKey: string) { this.toast('error', messageKey); }
  warning(messageKey: string) { this.toast('warning', messageKey); }
  info(messageKey: string) { this.toast('info', messageKey); }

  warningWithAction(messageKey: string, action: () => void) {
    this.messageKey.set(messageKey);
    this.titleKey.set('SWAL.WARNING');
    this.translate.get(['SWAL.WARNING', 'SWAL.ACTION_CONFIRM', 'SWAL.CANCEL', messageKey]).subscribe(translations => {
      Swal.fire({
        ...this.baseToastConfig(),
        icon: 'warning',
        title: translations['SWAL.WARNING'],
        text: translations[messageKey],
        showConfirmButton: true,
        confirmButtonText: translations['SWAL.ACTION_CONFIRM'],
        showCancelButton: true,
        cancelButtonText: translations['SWAL.CANCEL'],
        timer: undefined
      }).then(result => {
        if (result.isConfirmed) action();
      });
    });
  }

  modal(options: SweetAlertOptions) {
    return Swal.fire({
      ...options,
      didOpen: (modal: HTMLElement) => {
        modal.style.backgroundColor = 'var(--background)';
        modal.style.color = 'var(--text)';
        modal.style.border = '1px solid var(--primary)';
        modal.style.borderRadius = '0.75rem';

        const icon = modal.querySelector<HTMLElement>('.swal2-icon');
        if (icon) {
          icon.style.borderColor = 'var(--primary)';
          icon.style.color = 'var(--primary)';
          icon.style.backgroundColor = 'transparent';
        }

        const title = modal.querySelector<HTMLElement>('.swal2-title');
        if (title) title.style.color = 'var(--text)';

        const closeBtn = modal.querySelector<HTMLElement>('.swal2-close');
        if (closeBtn) closeBtn.style.color = 'var(--text)';
      }
    });
  }
}
