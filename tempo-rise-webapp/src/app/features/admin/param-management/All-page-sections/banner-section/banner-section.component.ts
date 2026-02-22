import { CommonModule } from '@angular/common';
import { Component, effect, EventEmitter, Input, OnInit, Output, Signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ParamSectionResponse } from '../../../../../core/models/params';
import { ParamsService } from '../../../../../core/services/params.service';
import { SwalService } from '../../../../../core/services/swal-service.service';


@Component({
  selector: 'app-banner-section',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './banner-section.component.html',
  styleUrl: './banner-section.component.css'
})
export class BannerSectionComponent implements OnInit{
  @Input() selectedSection!:  Signal<ParamSectionResponse | null>;
  @Output() loadPages = new EventEmitter<void>();
   contentForm!: FormGroup;
    constructor(private paramService: ParamsService,
              private toastr: SwalService,
              private fb: FormBuilder
  ) {effect(() => {
      const section = this.selectedSection();
      if (section) {
        this.loadSectionContent(section);
      }
    });}
   ngOnInit(): void {
    this.initContentForm();
  }
     private initContentForm(): void {
    this.contentForm = this.createContentForm();
  }
     private createContentForm(): FormGroup {
      return this.fb.group({
                  fix: [false],
                  speed: [null,[Validators.min(1),Validators.max(200)]],
                  texts: this.fb.array([])
                })         
  }
      private loadSectionContent(section: ParamSectionResponse): void {
    if (!section.contenuJson) this.contentForm = this.createContentForm();
else{
    try {
      const content = JSON.parse(section.contenuJson);
      if(content.texts?.length > 0)
        content.texts.forEach(() => this.addTexts());
      this.contentForm.patchValue(content);
    } catch (error) {
      console.error('Error parsing contenuJson:', error);
    }
    }
  }
    saveContent(): void {
    if (!this.contentForm.valid || !this.selectedSection()?.id) return;
      this.saveContentStandard(this.selectedSection()!);
  }

    private saveContentStandard(section: ParamSectionResponse): void {
    this.updateSection(section).subscribe({
      next: () => this.handleSaveSuccess(),
      error: (error) => this.handleSaveError(error)
    });
  }
    private updateSection(section: ParamSectionResponse) {
    const contenuJson = JSON.stringify(this.contentForm.value);
    
    return this.paramService.updateSection(section.id!, {
      ...section,
      contenuJson
    });
  }
    get textsArray(): FormArray {
      return this.contentForm.get('texts') as FormArray;
    }
  addTexts(): void {
    (this.textsArray).push(
      this.fb.group({text: [''] })
    );
  }
  removeTexts(index: number): void {
    
    this.textsArray.removeAt(index);
    
  } 
  onChange(event: Event): void {
    const value = event.target as HTMLInputElement;
    this.contentForm.patchValue({ fix: value.checked });
  }
    private handleSaveSuccess(): void {
    this.toastr.success('Contenu mis à jour avec succès');
    this.loadPages.emit();
  }

  private handleSaveError(error: any): void {
    console.error('Error during save process:', error);
    this.toastr.error('Erreur lors de la mise à jour');
  }
}
