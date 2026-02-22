import { Injectable, signal, WritableSignal } from '@angular/core';
import { ImageBlogpostService } from './image-blogpost.service';
interface PendingImage {
  file: File;
  base64: string;
}
@Injectable({
  providedIn: 'root'
})
export class QuillServices {
private _aboutPagePendingImages: WritableSignal<PendingImage[]> = signal<PendingImage[]>([]);
  aboutPagePendingImages = this._aboutPagePendingImages.asReadonly();
private _blogPostPendingImages: WritableSignal<PendingImage[]> = signal<PendingImage[]>([]);
  blogPostPendingImages = this._blogPostPendingImages.asReadonly();
  constructor(private imageBlogpostService: ImageBlogpostService) { 
  }

imageHandler(quill: any, isAboutPage: boolean) {
    
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    
    input.onchange = (event: any) => {
      
      if (!input.files || input.files.length === 0) {
        console.warn('No file selected');
        return;
      }
      
      const file = input.files[0];

      // Preview the image in Quill editor
      const reader = new FileReader();
      
      reader.onload = (e: any) => {
        
        if (!quill) {
          console.error('Quill instance is null');
          return;
        }
        
        const base64 = e.target.result;
        const range = quill.getSelection(true) || { index: quill.getLength() };
        
        
        // Insert the base64 image
        quill.insertEmbed(range.index, 'image', base64);
        quill.setSelection(range.index + 1);

        // Track this image for later replacement
        if (isAboutPage) {
          this._aboutPagePendingImages.update(current => {
          const updated = [...current, { file, base64 }];
          return updated;
        });
        }
        else{
          this._blogPostPendingImages.update(current => {
            const updated = [...current, { file, base64 }];
            return updated;
          });
        }
        
        
      };
      
      reader.onerror = (error) => {
        console.error('FileReader error:', error);
      };
      
      reader.readAsDataURL(file);
    };
    
    // Trigger the file picker
    input.click();
  }

  clearAboutPagePendingImages() {
    this._aboutPagePendingImages.set([]);
  }
  clearBlogPostPendingImages() {
    this._blogPostPendingImages.set([]);
  }

  // Get just the files for uploading
  getAboutPagePendingFiles(): File[] {
    return this._aboutPagePendingImages().map(img => img.file);
  }
  getBlogPostPendingFiles(): File[] {
    return this._blogPostPendingImages().map(img => img.file);
  }
  // Get the mapping of base64 to file for replacement
  getAboutPagePendingImagesMap(): Map<string, File> {
    const map = new Map<string, File>();
    this._aboutPagePendingImages().forEach(img => {
      map.set(img.base64, img.file);
    });
    return map;
  }
  getBlogPostPendingImagesMap(): Map<string, File> {
    const map = new Map<string, File>();
    this._blogPostPendingImages().forEach(img => {
      map.set(img.base64, img.file);
    });
    return map;
  }

  /**
   * Replace base64 images in HTML content with actual URLs
   * @param content - HTML content with base64 images
   * @param imageUrlMap - Map of base64 data to actual image URLs
   * @returns Updated HTML content with real URLs
   */
  replaceBase64WithUrls(content: string, imageUrlMap: Map<string, string>): string {
    let updatedContent = content;
    
    imageUrlMap.forEach((url, base64) => {
      // Replace all occurrences of the base64 string with the actual URL
      updatedContent = updatedContent.split(base64).join(url);
    });
    
    return updatedContent;
  }

  quillHandlers(quill: any, isAboutPage: boolean) {
    const table = quill.getModule('table');

    // Default table insert
    quill.getModule('toolbar').addHandler('table', () => table?.insertTable(2,2));

    // Custom buttons
    document.getElementById('insertTwoRowsBtn')?.addEventListener('click', () => {
      table?.insertRowBelow();
      table?.insertRowBelow();
    });

    document.getElementById('insertTwoTablesBtn')?.addEventListener('click', () => {
      const range = quill.getSelection(true);
      if(range){
        quill.insertEmbed(range.index, 'table', true);
        quill.insertText(range.index+1,'\n\n');
        quill.insertEmbed(range.index+2,'table', true);
      }
    });

    document.getElementById('mergeCellsBtn')?.addEventListener('click', () => table?.mergeCells());
    document.getElementById('unmergeCellsBtn')?.addEventListener('click', () => table?.unmergeCells());
    document.getElementById('insertRowAboveBtn')?.addEventListener('click', () => table?.insertRowAbove());
    document.getElementById('insertRowBelowBtn')?.addEventListener('click', () => table?.insertRowBelow());
    document.getElementById('insertColumnLeftBtn')?.addEventListener('click', () => table?.insertColumnLeft());
    document.getElementById('insertColumnRightBtn')?.addEventListener('click', () => table?.insertColumnRight());
    document.getElementById('deleteRowBtn')?.addEventListener('click', () => table?.deleteRow());
    document.getElementById('deleteColumnBtn')?.addEventListener('click', () => table?.deleteColumn());
    document.getElementById('deleteTableBtn')?.addEventListener('click', () => table?.deleteTable());

    // Custom image handler
    quill.getModule('toolbar').addHandler('image', () => this.imageHandler(quill, isAboutPage));
  }
}
