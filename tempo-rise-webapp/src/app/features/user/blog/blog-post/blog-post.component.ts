import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BlogService } from '../../../../core/services/blog.service';
import { BlogPost } from '../../../../core/models/BlogPost';
import { QuillModule } from "ngx-quill";


@Component({
  selector: 'app-blog-post',
  standalone: true,
  imports: [CommonModule, QuillModule],
  templateUrl: './blog-post.component.html'
})
export class BlogPostComponent implements OnInit {
  post?: BlogPost;

  constructor(
    private route: ActivatedRoute,
    private blogService: BlogService,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      this.blogService.getBlogById(id).subscribe({
        next: (post) => {
          this.post = post;
        },
        error: () => {
          this.post = undefined;
        }
      });
    });
  }

  // loadImages(blogPostId: number): void {
  //   this.imageService.getImageByProductId(blogPostId).subscribe({
  //     next: (imgs) => {
  //       this.images = imgs;

  //       if (imgs.length > 0) {
  //         // ✅ Définir la première image comme image principale
  //         this.post!.image = 'data:image/png;base64,' + imgs[0].image;
  //       } else {
  //         this.post!.image = 'https://via.placeholder.com/500x200?text=Pas+d\'image';
  //       }
  //     },
  //     error: () => {
  //       this.images = [];
  //       if (this.post) {
  //         this.post.image = 'https://via.placeholder.com/500x200?text=Erreur+chargement';
  //       }
  //     }
  //   });
  // }
}
