import { Component } from '@angular/core';
import { BlogService } from '../../../core/services/blog.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { BlogPost, imageBlogpost } from '../../../core/models/BlogPost';


@Component({
  standalone: true,
  selector: 'app-blog-admin',
  templateUrl: './blog-admin.component.html',
  styleUrls: ['./blog-admin.component.css'],
  imports: [CommonModule, RouterModule]
})
export class BlogAdminComponent {
  blogPosts: BlogPost[] = [];
  mainImage?: imageBlogpost[]=[];
  imageBaseUrl = environment.imageUrlBase;
  constructor(
    private blogService: BlogService,
  ) {}

  ngOnInit(): void {
    this.blogService.getAllBlogs().subscribe((posts) => {
      this.blogPosts = posts;
    });
  }

  stripHtml(html: string): string {
    const div = document.createElement('div');
    div.innerHTML = html;
    return div.textContent || div.innerText || '';
  }

  deleteBlog(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce blog ?')) {
      this.blogService.deleteBlog(id).subscribe(() => {
        this.blogPosts = this.blogPosts.filter(post => post.id !== id);
      });
    }
  }
}
