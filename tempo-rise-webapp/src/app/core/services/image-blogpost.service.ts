import { HttpClient } from '@angular/common/http';
import { Injectable, signal, WritableSignal } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { imageBlogpost } from '../models/BlogPost';

// Track temp images with their base64 data
interface PendingImage {
  file: File;
  base64: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageBlogpostService {
  private apiUrl = `${environment.apiUrl}/imageBlogPost`;
  private apiUrl1 = `${environment.apiUrl}/imageByBlogPostId`;

private _pendingImages: WritableSignal<PendingImage[]> = signal<PendingImage[]>([]);
  pendingImages = this._pendingImages.asReadonly();

  constructor(private http: HttpClient) {}

  uploadImage(file: File, blogPostId: number): Observable<any> {
    const formData = new FormData();
    formData.append('image', file);
    formData.append('blogPostId', blogPostId.toString());
    return this.http.post(`${this.apiUrl}`, formData);
  }

  deleteAllImages(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}`);
  }

  deleteImage(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getAllImages(): Observable<imageBlogpost[]> {
    return this.http.get<imageBlogpost[]>(`${this.apiUrl}`);
  }

  getImageById(id: number): Observable<imageBlogpost> {
    return this.http.get<imageBlogpost>(`${this.apiUrl}/${id}`);
  }

  updateProduct(id: number, ImageBlogpost: imageBlogpost): Observable<imageBlogpost> {
    return this.http.put<imageBlogpost>(`${this.apiUrl}/${id}`, ImageBlogpost);
  }

  getImageByProductId(id: number): Observable<imageBlogpost[]> {
    return this.http.get<imageBlogpost[]>(`${this.apiUrl1}/${id}`);
  }

// imageHandler(quill: any) {
//     console.log('imageHandler called');
    
//     const input = document.createElement('input');
//     input.setAttribute('type', 'file');
//     input.setAttribute('accept', 'image/*');
    
//     input.onchange = (event: any) => {
//       console.log('File input changed', event);
      
//       if (!input.files || input.files.length === 0) {
//         console.warn('No file selected');
//         return;
//       }
      
//       const file = input.files[0];
//       console.log('File selected:', file.name, file.size);

//       // Preview the image in Quill editor
//       const reader = new FileReader();
      
//       reader.onload = (e: any) => {
//         console.log('FileReader loaded');
        
//         if (!quill) {
//           console.error('Quill instance is null');
//           return;
//         }
        
//         const base64 = e.target.result;
//         const range = quill.getSelection(true) || { index: quill.getLength() };
        
//         console.log('Inserting image at index:', range.index);
        
//         // Insert the base64 image
//         quill.insertEmbed(range.index, 'image', base64);
//         quill.setSelection(range.index + 1);

//         // Track this image for later replacement
//         this._pendingImages.update(current => {
//           const updated = [...current, { file, base64 }];
//           console.log('Pending images updated. New count:', updated.length);
//           return updated;
//         });
        
//         console.log('Final pending images count:', this._pendingImages().length);
//       };
      
//       reader.onerror = (error) => {
//         console.error('FileReader error:', error);
//       };
      
//       reader.readAsDataURL(file);
//     };
    
//     // Trigger the file picker
//     input.click();
//     console.log('File picker opened');
//   }

//   clearPendingImages() {
//     console.log('Clearing pending images. Current count:', this._pendingImages().length);
//     this._pendingImages.set([]);
//   }

//   // Get just the files for uploading
//   getPendingFiles(): File[] {
//     return this._pendingImages().map(img => img.file);
//   }

//   // Get the mapping of base64 to file for replacement
//   getPendingImagesMap(): Map<string, File> {
//     const map = new Map<string, File>();
//     this._pendingImages().forEach(img => {
//       map.set(img.base64, img.file);
//     });
//     return map;
//   }

//   /**
//    * Replace base64 images in HTML content with actual URLs
//    * @param content - HTML content with base64 images
//    * @param imageUrlMap - Map of base64 data to actual image URLs
//    * @returns Updated HTML content with real URLs
//    */
//   replaceBase64WithUrls(content: string, imageUrlMap: Map<string, string>): string {
//     let updatedContent = content;
    
//     imageUrlMap.forEach((url, base64) => {
//       // Replace all occurrences of the base64 string with the actual URL
//       updatedContent = updatedContent.split(base64).join(url);
//     });
    
//     return updatedContent;
//   }
}