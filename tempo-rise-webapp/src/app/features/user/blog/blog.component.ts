import { Component, computed, inject, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BlogService } from '../../../core/services/blog.service';
import { TranslateModule } from '@ngx-translate/core';
import { ExpandLongTextComponent } from "../../../shared/components/expand-long-text/expand-long-text.component";
import { BlogPost } from '../../../core/models/BlogPost';
import { environment } from '../../../../environments/environment';
import { SpinnerComponent } from "../../../shared/components/spinner/spinner.component";
import { ParamsService } from '../../../core/services/params.service';


interface mainSection{
  title: string;
}

@Component({
    selector: 'app-blog',
    imports: [CommonModule, RouterLink, TranslateModule, ExpandLongTextComponent,SpinnerComponent],
    templateUrl: './blog.component.html'
})
export class BlogComponent {
  private paramService = inject(ParamsService);
  activeBlogsSection = computed(() => this.paramService.blogsPageSections().filter(section => section.active))
  activeBlogsMainSection = computed(() => this.activeBlogsSection().find(section => section.type === 'OUR_BLOGS'))
  mainContent:Signal<mainSection | null> = computed(() => {
    const content = this.activeBlogsMainSection()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title
      };
    }
    return null;
  })
  
  blogPosts: BlogPost[] = [];
  isLoading = false;
  imageBaseUrl = environment.imageUrlBase;
  constructor(private blogService: BlogService) {}

  ngOnInit(): void {
    this.isLoading = true
    this.blogService.getAllBlogs().subscribe({next:(posts) => {
      const publishedPosts = posts.filter(post => post.status === 'PUBLIER');
      this.blogPosts = publishedPosts;
      this.isLoading = false
    },
    error: (err) => {
      console.error(err);
      this.isLoading = false
    }},
    );
    this.paramService.getSectionsByPageType('BLOG').subscribe();
  }
  stripHtml(html: string): string {
    const div = document.createElement('div');
    div.innerHTML = html;
    return div.textContent || div.innerText || '';
  }
  
}