import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BlogService } from '../../../../core/services/blog.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ImageBlogpostService } from '../../../../core/services/image-blogpost.service';
import { QUILL_MODULES } from '../../../../shared/configs/quill-config';
import { environment } from '../../../../../environments/environment';
import { CommonModule } from '@angular/common';
import { QuillModule } from 'ngx-quill';
import { UploadImagesService } from '../../../../core/services/upload-images.service';
import { QuillServices } from '../../../../core/services/quill.service';

@Component({
  selector: 'app-add-blog',
  templateUrl: './add-blog.component.html',
  styleUrls: ['./add-blog.component.css'],
  imports: [CommonModule, ReactiveFormsModule, QuillModule],
})
export class AddBlogComponent {
  blogForm: FormGroup;
  successMessage = '';
  selectedFile: File | null = null;
  quill: any;
  imageBaseUrl = environment.imageUrlBase;
  modules = QUILL_MODULES;
  table :any;
  selectedFileName?: string = '';
  isSubmitting: boolean = false;

  constructor(
    private fb: FormBuilder,
    private blogService: BlogService,
    private imageBlogpostService: ImageBlogpostService,
    private authService: AuthService,
    private uploadService: UploadImagesService,
    private quillService: QuillServices
  ) {
    this.blogForm = this.fb.group({
      titre: ['', Validators.required],
      contenu: ['', Validators.required],
      status: ['BROUILLON', Validators.required],
    });
  }

  // Called when ngx-quill editor is ready
  onEditorCreated(quill: any) {
    this.quill = quill;
    this.quillService.quillHandlers(quill,false);
    // Attach custom image handler
  }
  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
      this.selectedFileName = this.selectedFile?.name;
    }
  }

  submitBlog() {
    if (!this.blogForm.valid) return;

    const auteur = this.authService.getCurrentUser?.id;
    if (!auteur) return console.error('Utilisateur non connecté');
    this.isSubmitting = true;
    // Patch the form with current Quill content
    this.blogForm.patchValue({ contenu: this.quill.root.innerHTML });

    const pendingFiles = this.quillService.getBlogPostPendingFiles();
    const imageMap = this.quillService.getBlogPostPendingImagesMap();

    if (pendingFiles.length === 0) {
      this.createBlogPost(auteur);
    } else {
      this.uploadImagesAndCreatePost(auteur, pendingFiles, imageMap);
    }
  }

  private createBlogPost(auteur: any) {
    const newBlog = { ...this.blogForm.value, auteur };
    this.blogService.addBlog(newBlog).subscribe({
      next: (createdBlog) => {
        const blogId = createdBlog.id;
        // Upload main image if exists
        if (this.selectedFile) {
          this.imageBlogpostService.uploadImage(this.selectedFile, blogId)
            .subscribe();
        }

        this.resetForm();
        this.successMessage = 'Article ajouté avec succès ! 🎉';
        this.isSubmitting = false;
      },
      error: (err) => {console.error('Blog creation failed', err);
        this.isSubmitting = false;
      },
    });
  }

  private uploadImagesAndCreatePost(auteur: any, files: File[], imageMap: Map<string, File>) {
    // Create temporary blog to get ID
    const tempBlog = { ...this.blogForm.value, auteur };
    const base64ToUrlMap = new Map<string, string>();
    let uploadedCount = 0;
    imageMap.forEach((file, base64) => {
      this.uploadService.uploadImage(file).subscribe({next:(response) => {
      const imageUrl= this.imageBaseUrl + response.filename;
      base64ToUrlMap.set(base64, imageUrl);
      uploadedCount++;
      if(uploadedCount === imageMap.size){
        this.updateBlogContentWithUrls(tempBlog, base64ToUrlMap, auteur);
      }
      this.isSubmitting = false;
      
    },
      error: err => {console.error('Image upload failed', err);
         this.isSubmitting = false;
      }
    })});
  }

  private updateBlogContentWithUrls(tempBlog: any, base64ToUrlMap: Map<string, string>, auteur: any) {
    let content = tempBlog.contenu;
    content = this.quillService.replaceBase64WithUrls(content, base64ToUrlMap);

    const updatedBlog = { ...this.blogForm.value, contenu: content, auteur };

    this.blogService.addBlog(updatedBlog).subscribe({
      next: (savedBlog) => {
        if(this.selectedFile){
          this.imageBlogpostService.uploadImage(this.selectedFile, savedBlog.id)
            .subscribe({ next: () => {
          this.successMessage = 'Article ajouté avec succès ! 🎉';
          this.resetForm();
          this.quillService.clearBlogPostPendingImages();
            }});
        }
        else{
        this.successMessage = 'Article ajouté avec succès ! 🎉';
        this.resetForm();
        this.quillService.clearBlogPostPendingImages();
        }
        this.isSubmitting = false;
      },
      error: (err) => {console.error('Blog update failed', err);
        this.isSubmitting = false;
      },
    });
  }

  private resetForm() {
    this.blogForm.reset({
      titre: '',
      contenu: '',
      status: 'BROUILLON',
    });
    this.selectedFile = null;
  }
    cancelForm(): void {
      this.resetForm();
      // Navigate back or to blog list
      // this.router.navigate(['/blogs']);
    
  }
}
