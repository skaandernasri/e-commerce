import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BlogService } from '../../../../core/services/blog.service';
import { ActivatedRoute } from '@angular/router';
import { QuillModule } from 'ngx-quill';
import Quill from 'quill';
import { ImageBlogpostService } from '../../../../core/services/image-blogpost.service';
import ImageSpec from '@enzedonline/quill-blot-formatter2';
import { QUILL_MODULES } from '../../../../shared/configs/quill-config';
import { BlogPost } from '../../../../core/models/BlogPost';
import { environment } from '../../../../../environments/environment';
import { UploadImagesService } from '../../../../core/services/upload-images.service';
import { QuillServices } from '../../../../core/services/quill.service';

Quill.register('modules/imageResize', ImageSpec);

@Component({
  selector: 'app-update-blog',
  standalone: true,
  templateUrl: './update-blog.component.html',
  styleUrls: ['./update-blog.component.css'],
  imports: [CommonModule, ReactiveFormsModule, QuillModule] 
})
export class UpdateBlogComponent {
  blogForm: FormGroup;
  quill: any;
  modules = QUILL_MODULES;
  blogId: number | null = null;
  selectedFile: File | null = null;
  blog?: BlogPost;
  successMessage = '';
  imageBaseUrl = environment.imageUrlBase;

  constructor(
    private fb: FormBuilder,
    private blogService: BlogService,
    private route: ActivatedRoute,
    private imageBlogpostService: ImageBlogpostService,
    private uploadService: UploadImagesService,
    private quillService: QuillServices
  ) {
    this.blogForm = this.fb.group({
      titre: ['', Validators.required],
      contenu: ['', Validators.required],
      status: ['BROUILLON', Validators.required]
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.blogId = +params['id'];
      if (this.blogId) {
        this.blogService.getBlogById(this.blogId).subscribe(blog => {
          this.blog = blog;
          this.blogForm.patchValue({
            ...blog,
          });
        });
      }
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
    }
  }

  submitUpdate() {
    if (!this.blogForm.valid || this.blogId === null) return;

    // Patch form with current Quill content
    this.blogForm.patchValue({ contenu: this.quill.root.innerHTML });

    const pendingFiles = this.quillService.getBlogPostPendingFiles();
    const imageMap = this.quillService.getBlogPostPendingImagesMap();

    if (pendingFiles.length === 0) {
      this.updateBlogPost();
    } else {
      this.uploadImagesAndUpdatePost(imageMap);
    }
  }

  private updateBlogPost() {
    if (!this.blogId) return;

    const updatedBlog = { ...this.blogForm.value, id: this.blogId };

    this.blogService.updateBlog(this.blogId, updatedBlog).subscribe({
      next: () => {
        this.successMessage = 'Article mis à jour avec succès ! ✅';
        this.uploadSelectedImage();
        this.resetForm();
      },
      error: err => console.error('Blog update failed', err)
    });
  }

  private uploadImagesAndUpdatePost(imageMap: Map<string, File>) {
    if (!this.blogId) return;

    let uploadedCount = 0;
    const base64ToUrlMap = new Map<string, string>();
    imageMap.forEach((file, base64) => {
      this.uploadService.uploadImage(file).subscribe({
        next: (response) => {
          const imageUrl = this.imageBaseUrl + response.filename;
          base64ToUrlMap.set(base64, imageUrl);
          uploadedCount++;

          if (uploadedCount === imageMap.size) {
            this.updateBlogContentWithUrls(base64ToUrlMap);
          }
        },
        error: err => console.error('Image upload failed', err)
      });
    });
  }

  private updateBlogContentWithUrls(base64ToUrlMap: Map<string, string>) {
    if (!this.blogId) return;

    let content = this.blogForm.get('contenu')?.value;
    content = this.quillService.replaceBase64WithUrls(content, base64ToUrlMap);

    const updatedBlog = { ...this.blogForm.value, contenu: content, id: this.blogId };

    this.blogService.updateBlog(this.blogId, updatedBlog).subscribe({
      next: () => {
        this.successMessage = 'Article mis à jour avec succès ! ✅';
        this.uploadSelectedImage();
        this.resetForm();
      },
      error: err => console.error('Blog update failed', err)
    });
  }

  private uploadSelectedImage() {
    if (this.selectedFile && this.blogId) {
      if (this.blog?.image && this.blog.image.length > 0) {
        this.imageBlogpostService.deleteImage(this.blog.image[0].id!).subscribe();
      }
      this.imageBlogpostService.uploadImage(this.selectedFile, this.blogId)
        .subscribe({error: err => console.error(err) });
    }
  }

  private resetForm() {
    this.blogForm.reset({
      titre: '',
      contenu: '',
      status: 'BROUILLON'
    });
    this.selectedFile = null;
    this.quillService.clearBlogPostPendingImages();
  }
}
