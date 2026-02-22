import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigGlobal } from '../../../../core/models/configGlobal';
import { ConfigGlobalService } from '../../../../core/services/config-global.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-global-params',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './global-params.component.html',
  styleUrl: './global-params.component.css'
})
export class GlobalParamsComponent implements OnInit {
configForm!: FormGroup;
  isLoading = signal(false);
  isSaving = signal(false);
  currentConfig = this.configService.configGlobal
  saveSuccess = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private configService: ConfigGlobalService
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadConfig();
  }

  private initForm(): void {
    this.configForm = this.fb.group({
      valeurLivraison: [0, [Validators.required, Validators.min(0)]],
      seuilLivraisonGratuite: [0, [Validators.required, Validators.min(0)]]
    });
  }

  loadConfig(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.configService.getConfigGlobal().subscribe({
      next: (config) => {
        this.configForm.patchValue({
          valeurLivraison: config.valeurLivraison,
          seuilLivraisonGratuite: config.seuilLivraisonGratuite
        });
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading config:', error);
        this.errorMessage.set('Erreur lors du chargement de la configuration');
        this.isLoading.set(false);
      }
    });
  }

  saveConfig(): void {
    if (this.configForm.invalid) {
      return;
    }

    this.isSaving.set(true);
    this.saveSuccess.set(false);
    this.errorMessage.set(null);

    const configData: ConfigGlobal = {
      valeurLivraison: this.configForm.value.valeurLivraison,
      seuilLivraisonGratuite: this.configForm.value.seuilLivraisonGratuite
    };

    this.configService.createOrUpdateConfigGlobal(configData).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.saveSuccess.set(true);
        
        // Hide success message after 3 seconds
        setTimeout(() => {
          this.saveSuccess.set(false);
        }, 3000);
      },
      error: (error) => {
        console.error('Error saving config:', error);
        this.errorMessage.set('Erreur lors de la sauvegarde de la configuration');
        this.isSaving.set(false);
      }
    });
  }

  resetForm(): void {
    if (this.currentConfig()) {
      this.configForm.patchValue({
        valeurLivraison: this.currentConfig()!.valeurLivraison,
        seuilLivraisonGratuite: this.currentConfig()!.seuilLivraisonGratuite
      });
    }
  }

  get valeurLivraisonControl() {
    return this.configForm.get('valeurLivraison');
  }

  get seuilLivraisonGratuiteControl() {
    return this.configForm.get('seuilLivraisonGratuite');
  }
}
