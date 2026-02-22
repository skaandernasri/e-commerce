import { Component, effect, EventEmitter, Input, OnInit, Output, Signal } from '@angular/core';
import { ParamSectionResponse } from '../../../../../core/models/params';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ParamsService } from '../../../../../core/services/params.service';
import { SwalService } from '../../../../../core/services/swal-service.service';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { UploadImageComponent } from "../../../../../shared/components/upload-image/upload-image.component";
import { UploadImagesService } from '../../../../../core/services/upload-images.service';
import { forkJoin, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-right-banner-section',
  imports: [CommonModule, FormsModule,
    ReactiveFormsModule, LucideAngularModule,
     UploadImageComponent],
  templateUrl: './right-banner-section.component.html',
  styleUrl: './right-banner-section.component.css'
})
export class RightBannerSectionComponent implements OnInit {
  @Input() selectedSection!:  Signal<ParamSectionResponse | null>;
  @Output() loadPages = new EventEmitter<void>();
   contentForm!: FormGroup;
   selectedFiles: Map<number, File> = new Map<number, File>();
   imagesPreview: Map<number, string> = new Map<number, string>();
   filesToRemove: string[] = [];
    constructor(private paramService: ParamsService,
              private toastr: SwalService,
              private fb: FormBuilder,
              private imageUploadService: UploadImagesService
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
                  bar:this.fb.array([]),
                })         
  }

      private loadSectionContent(section: ParamSectionResponse): void {
    if (!section.contenuJson) this.contentForm = this.createContentForm();
else{
    try {
      const content = JSON.parse(section.contenuJson);      
      if (content.bar?.length > 0)
        content.bar.forEach(() => this.addBar());
      this.contentForm.patchValue(content);
    } catch (error) {
      console.error('Error parsing contenuJson:', error);
    }
    }
  }
    saveContent(): void {
    if (!this.contentForm.valid || !this.selectedSection()?.id) return;

    const hasImages = this.selectedFiles.size > 0;
    
    if (hasImages){
      this.saveContentWithImages(this.selectedSection()!);
    }
    else
      this.saveContentStandard(this.selectedSection()!);
  }

    private saveContentStandard(section: ParamSectionResponse): void {
    this.updateSection(section).subscribe({
      next: () => this.handleSaveSuccess(),
      error: (error) => this.handleSaveError(error)
    });
  }
  private saveContentWithImages(section: ParamSectionResponse): void {
    const uploadObservables = [
      ...this.createImage()
    ].filter(obs => obs !== null);
    
    forkJoin(uploadObservables).pipe(
      switchMap(() => {    
        return this.updateSection(section);
}
    )
    ).subscribe({
      next: () => this.handleSaveSuccess(),
      error: (error) => this.handleSaveError(error)
    });
  }
  deleteOldImage(){
    if(this.filesToRemove.length > 0)      
      this.filesToRemove.forEach(imageUrl => this.imageUploadService.deleteImage(imageUrl).subscribe({
        error: (error) => console.error('Error deleting image:', error)
        }));
    this.filesToRemove = [];
  }
    private createImage() {
    return Array.from(this.selectedFiles).map(([imageIndex, file]) => {
      return this.imageUploadService.uploadImage(file).pipe(
        switchMap((response: any) => {
          this.updateImageUrl(imageIndex, response.filename);  
          this.toastr.success(`Image ${imageIndex + 1} chargée avec succès`);
          return of(response);
        })
      );
    });
  }
  private updateImageUrl(index: number, filename: any) {
    const imageUrl = filename;    
    this.barArray.at(index).get('imageUrl')?.setValue(imageUrl); 
  }
    private updateSection(section: ParamSectionResponse) {
    const contenuJson = JSON.stringify(this.contentForm.value);    
    return this.paramService.updateSection(section.id!, {
      ...section,
      contenuJson
    });
  }
    get barArray(): FormArray {return this.contentForm.get('bar') as FormArray;}
    addBar(): void {      
    this.barArray.push(
      this.fb.group({imageUrl: [''], text: ['',[Validators.required,Validators.minLength(2),Validators.maxLength(30)]], url: [''] })
    );
  }
    removeBar(index: number): void {
      (this.barArray).removeAt(index);
    }
  onFileSelected(file: File, index: number): void {
    if (!file) return;
    this.selectedFiles.set(index, file);
    this.imagesPreview.set(index, URL.createObjectURL(file));
    if(this.barArray.controls[index].get('imageUrl')?.value)
      this.filesToRemove.push(this.barArray.controls[index].get('imageUrl')?.value);
  }
    removeFile(index: number): void {
    if(this.barArray.controls[index].get('imageUrl')?.value)
      this.filesToRemove.push(this.barArray.controls[index].get('imageUrl')?.value);
    this.selectedFiles.delete(index);
    this.imagesPreview.delete(index);
    this.barArray.controls[index].get('imageUrl')?.setValue('');
  }
    private handleSaveSuccess(): void {
    this.deleteOldImage();
    this.toastr.success('Contenu mis à jour avec succès');
    this.loadPages.emit();
  }

  private handleSaveError(error: any): void {
    console.error('Error during save process:', error);
    this.toastr.error('Erreur lors de la mise à jour');
  }
  


}

