import { CommonModule } from '@angular/common';
import { Component, effect, EventEmitter, Input, OnInit, Output, Signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ParamSectionResponse } from '../../../../../core/models/params';
import { ParamsService } from '../../../../../core/services/params.service';
import { SwalService } from '../../../../../core/services/swal-service.service';

@Component({
  selector: 'app-content8-section',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './content8-section.component.html',
  styleUrl: './content8-section.component.css'
})
export class Content8SectionComponent implements OnInit{
  @Input() selectedSection!:  Signal<ParamSectionResponse | null>;
  @Output() loadPages = new EventEmitter<void>();
   contentForm!: FormGroup;
    constructor(private paramService: ParamsService,
              private toastr: SwalService,
              private fb: FormBuilder
  ) {effect(() => this.loadSectionContent(this.selectedSection()!));}
   ngOnInit(): void {
    this.initContentForm();
    //this.loadSectionContent(this.selectedSection()!);
  }
     private initContentForm(): void {
    this.contentForm = this.createContentForm();
  }
     private createContentForm(): FormGroup {
      return this.fb.group({
                  title: [''], 
                  subtitle: [''], 
                  buttonText: [''], 
                  buttonLink: ['']
                })         
  }
      private loadSectionContent(section: ParamSectionResponse): void {
    if (!section.contenuJson) this.contentForm = this.createContentForm();
else{
    try {
      const content = JSON.parse(section.contenuJson);
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
    private handleSaveSuccess(): void {
    this.toastr.success('Contenu mis à jour avec succès');
    this.loadPages.emit();
  }

  private handleSaveError(error: any): void {
    console.error('Error during save process:', error);
    this.toastr.error('Erreur lors de la mise à jour');
  }
}
