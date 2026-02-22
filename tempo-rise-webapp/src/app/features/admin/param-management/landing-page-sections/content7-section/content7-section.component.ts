import { CommonModule } from '@angular/common';
import { Component, effect, EventEmitter, Input, OnInit, Output, Signal, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ImageTextPair, ParamSectionResponse } from '../../../../../core/models/params';
import { environment } from '../../../../../../environments/environment';
import { ParamsService } from '../../../../../core/services/params.service';
import { UploadImagesService } from '../../../../../core/services/upload-images.service';
import { SwalService } from '../../../../../core/services/swal-service.service';
import { forkJoin, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-content7-section',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './content7-section.component.html',
  styleUrl: './content7-section.component.css'
})
export class Content7SectionComponent  implements OnInit{
  @Input() selectedSection!:  Signal<ParamSectionResponse | null>;
  @Output() loadPages = new EventEmitter<void>();
  contentForm!: FormGroup;
  imagesBaseUrl = environment.imageUrlBase;
  selectedFiles = new Map<number, File>();
  imagePreviews = new Map<number, string>();
  readonly limitImages = signal(6);
    constructor(
    private fb: FormBuilder,
    private paramService: ParamsService,
    private imageUploadService: UploadImagesService,
    private toastr: SwalService
  ) {effect(() => this.loadSectionContent(this.selectedSection()!));}
  ngOnInit(): void {
    this.initContentForm();
   // this.loadSectionContent(this.selectedSection()!);
  }
  private initContentForm(): void {
    this.contentForm = this.createContentForm();
  }
  private createContentForm(): FormGroup {
    return this.fb.group({
      title: [''], subtitle: [''], imagesAndText: this.fb.array([])
    });
  }
    loadSectionContent(section: ParamSectionResponse): void {
    if (!section.contenuJson) this.contentForm = this.createContentForm();
else{
    try {
      const content = JSON.parse(section.contenuJson);
      
      if (content.imagesAndText?.length > 0) {
        // Clear existing previews and files
        this.imagePreviews.clear();
        this.selectedFiles.clear();
        
        content.imagesAndText.forEach(() => this.addImageTextPair());
      }
      
      this.contentForm.patchValue(content);
    } catch (error) {
      console.error('Error parsing contenuJson:', error);
    }
    }
  }
  saveContent(): void {
    if (!this.contentForm.valid || !this.selectedSection()?.id) return;

    const hasImages = this.selectedFiles.size > 0;
    
    if (hasImages) {
      this.saveContentWithImages(this.selectedSection()!);
    } else {
      this.saveContentStandard(this.selectedSection()!);
    }
  }
  saveContentWithImages(section: ParamSectionResponse): void {
      const uploadObservables = Array.from(this.selectedFiles).map((file) => {
        if (!file) return of(null);
        
        return this.imageUploadService.uploadImage(file[1]).pipe(
          switchMap((response: any) => {
            const imageTextItem = this.imagesAndText.value.find((item: ImageTextPair) => item.index === file[0]);
            if (imageTextItem) {
              imageTextItem.imageUrl = response.filename;
            }
            this.toastr.success(`Image ${file[0] + 1} chargée avec succès`);
            return of(response);
          })
        );
      });
  
      forkJoin(uploadObservables).pipe(
        switchMap(() => {
          const contenuJson = JSON.stringify(this.contentForm.value);
          return this.paramService.updateSection(section.id!, {
            ...section,
            contenuJson
          });
        })
      ).subscribe({
        next: () => {
          this.handleSaveSuccess();
        },
        error: (error) => {
          this.handleSaveError(error);
        }
      });
    }
  
    saveContentStandard(section: ParamSectionResponse): void {
      const contenuJson = JSON.stringify(this.contentForm.value);
      
      this.paramService.updateSection(section.id!, {
        ...section,
        contenuJson
      }).subscribe({
        next: () => {
          this.handleSaveSuccess();
        },
        error: (error) => {
          this.handleSaveError(error);
        }
      });
    }
    addImageTextPair(): void {
    if (this.imagesAndText.length >= this.limitImages()) return;
    this.imagesAndText.push(
      this.fb.group({
        index: [this.imagesAndText.length],
        imageUrl: [''],
        title: [''],
        subtitle: ['']
      })
    );
  }
   removeImageTextPair(index: number, imageInFormIndex: number): void {
    const imageUrl = this.imagesAndText.value[index]?.imageUrl;
    
    if (imageUrl) {
      this.imageUploadService.deleteImage(imageUrl).subscribe();
    }
    
    this.imagesAndText.removeAt(index);
    this.reorderImagesAndText();
    this.selectedFiles.delete(imageInFormIndex);
    this.imagePreviews.delete(index);
  }
  reorderImagesAndText(): void {
    this.imagesAndText.value.forEach((item: any, index: number) => {
      item.index = index;
    });
  }
   get imagesAndText(): FormArray {
    return this.contentForm.get('imagesAndText') as FormArray;
  }
    onFileEvent(event: Event, index: number, imageInFormIndex: number): void {
    const file = this.getFileFromEvent(event);
    if (!file) return;

    this.selectedFiles.set(imageInFormIndex, file);
    this.imagePreviews.set(index, URL.createObjectURL(file));
  }
getFileFromEvent(event: Event): File | null {
    const input = event.target as HTMLInputElement;
    return input.files && input.files.length > 0 ? input.files[0] : null;
  }
  removeFile(index: number, imageInFormIndex: number): void {
    this.selectedFiles.delete(imageInFormIndex);
    this.imagePreviews.delete(index);
    this.imagesAndText.at(index).patchValue({ imageUrl: '' });
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