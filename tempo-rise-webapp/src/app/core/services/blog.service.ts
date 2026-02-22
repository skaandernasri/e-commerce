import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BlogPost } from '../models/BlogPost';



@Injectable({
  providedIn: 'root'
})
export class BlogService {
  private apiUrl = `${environment.apiUrl}/articles`;

  constructor(private http: HttpClient) {}


  getAllBlogs(): Observable<BlogPost[]> {
    return this.http.get<BlogPost[]>(this.apiUrl);
  }
  addBlog(blog: BlogPost): Observable<BlogPost> {
    return this.http.post<BlogPost>(this.apiUrl, blog);
  }
  deleteBlog(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  getBlogById(id: number): Observable<BlogPost> {
    return this.http.get<BlogPost>(`${this.apiUrl}/${id}`);
  }
  updateBlog(id: number, blog: BlogPost): Observable<BlogPost> {
    return this.http.put<BlogPost>(`${this.apiUrl}/${id}`, blog);
  }
  
}
