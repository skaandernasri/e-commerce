import { Component, EventEmitter, Input, Output } from '@angular/core';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-upload-image',
  imports: [],
  templateUrl: './upload-image.component.html',
  styleUrl: './upload-image.component.css'
})
export class UploadImageComponent {
@Input() titre: string = '';
@Input() buttonText: string = 'Upload';
@Input() imageUrl: string = '';
@Output() onFileSelected = new EventEmitter<File>();
@Output() onFileRemoved = new EventEmitter<void>();
selectedFile: File | null = null;
imagePreview: string = '';
imagesBaseUrl = environment.imageUrlBase;
  onFileEvent(event: Event): void {
    const file = this.getFileFromEvent(event);
    if (!file) return;
    if (!file.type.startsWith('image/')) return; 

    this.selectedFile = file;
    this.imagePreview = URL.createObjectURL(file);
    this.onFileSelected.emit(file);
  }
    private getFileFromEvent(event: Event): File | null {
    const input = event.target as HTMLInputElement;
    return input.files?.[0] ?? null;
  }
  removeFile(): void { {
      this.selectedFile = null;
      this.imagePreview = '';
      this.imageUrl = '';
      this.onFileRemoved.emit();
    }
  }
  
}
