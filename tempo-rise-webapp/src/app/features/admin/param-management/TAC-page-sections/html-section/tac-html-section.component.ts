import { Component, effect, EventEmitter, Input, OnInit, Output, Signal } from '@angular/core';
import { ParamSectionResponse } from '../../../../../core/models/params';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { QUILL_MODULES } from '../../../../../shared/configs/quill-config';
import { environment } from '../../../../../../environments/environment';
import { ParamsService } from '../../../../../core/services/params.service';
import { SwalService } from '../../../../../core/services/swal-service.service';
import { QuillServices } from '../../../../../core/services/quill.service';
import { UploadImagesService } from '../../../../../core/services/upload-images.service';
import { QuillModule } from 'ngx-quill';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tac-html-section',
  imports: [CommonModule,FormsModule,ReactiveFormsModule,QuillModule],
  templateUrl: './tac-html-section.component.html',
  styleUrl: './tac-html-section.component.css'
})
export class TacHtmlSectionComponent implements OnInit {
  @Input() selectedSection!:  Signal<ParamSectionResponse | null>;
  @Output() loadPages = new EventEmitter<void>();
  contentForm!: FormGroup;
  modules = QUILL_MODULES;
  quill: any;
  imageBaseUrl = environment.imageUrlBase
   constructor(
    private fb: FormBuilder,
    private paramService: ParamsService,
    private toastr: SwalService,
    private quillService: QuillServices,
    private uploadService: UploadImagesService
  ) {
    effect(() => this.loadSectionContent(this.selectedSection()!));
  }
    ngOnInit(): void {
    this.initContentForm();
    // this.loadSectionContent(this.selectedSection()!);
  }
    onEditorCreated(quill: any) {
    this.quill = quill;
    this.quillService.quillHandlers(quill,true);
    // Attach custom image handler
  }
    private initContentForm(): void {
    this.contentForm = this.createContentForm();


  }
    private loadSectionContent(section: ParamSectionResponse): void {
    if (!section.contenuHtml) this.contentForm = this.createContentForm();
      else{
    try {
      this.contentForm.patchValue({ contenuHtml: section.contenuHtml });
    } catch (error) {
      console.error('Error parsing contenuJson:', error);
    }
    }
  }
    private createContentForm(): FormGroup {
      return this.fb.group({
                  contenuHtml: ['']
                })          
  }

  saveContent(): void {
    if (!this.contentForm.valid || !this.selectedSection()?.id) return;
    this.contentForm.patchValue({ contenuHtml: this.quill.root.innerHTML });
    const pendingFiles = this.quillService.getAboutPagePendingFiles();
    const imageMap = this.quillService.getAboutPagePendingImagesMap();

    if (pendingFiles.length === 0) {
      this.saveContentStandard(this.selectedSection()!);
    } else {
      this.uploadImagesAndUpdateContent(imageMap);
    }
  }
    
    private saveContentStandard(section: ParamSectionResponse): void {
    this.updateSection({...section, contenuHtml: this.contentForm.get('contenuHtml')?.value}).subscribe({
      next: () => this.handleSaveSuccess(),
      error: (error) => this.handleSaveError(error)
    });
  }
    private updateSection(section: ParamSectionResponse) {
    return this.paramService.updateSection(section.id!, {
      ...section,
      contenuHtml: this.contentForm.get('contenuHtml')?.value
    });
  }
  private uploadImagesAndUpdateContent(imageMap: Map<string, File>): void {
    let uploadedCount = 0;
    const base64ToUrlMap = new Map<string, string>();
    imageMap.forEach((file, base64) => {
      this.uploadService.uploadImage(file).subscribe({
        next: (response) => {
          const imageUrl = this.imageBaseUrl + response.filename;
          base64ToUrlMap.set(base64, imageUrl);
          uploadedCount++;

          if (uploadedCount === imageMap.size) {
            this.updateContentWithUrls(base64ToUrlMap);
          }
        },
        error: err => console.error('Image upload failed', err)
      });
    });
  }
  private updateContentWithUrls(base64ToUrlMap: Map<string, string>) {

    let content = this.contentForm.get('contenuHtml')?.value;
    content = this.quillService.replaceBase64WithUrls(content, base64ToUrlMap);

    this.contentForm.patchValue({ contenuHtml: content });
    this.saveContentStandard(this.selectedSection()!);
  }
    private handleSaveSuccess(): void {
    this.toastr.success('Contenu mis à jour avec succès');
    this.quillService.clearAboutPagePendingImages();
    this.loadPages.emit();
  }

  private handleSaveError(error: any): void {
    console.error('Error during save process:', error);
    this.toastr.error('Erreur lors de la mise à jour');
  }
}
