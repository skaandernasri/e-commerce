import { HttpClient } from '@angular/common/http';
import { Injectable, signal, WritableSignal } from '@angular/core';
import { Category } from '../models/category';
import { catchError, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private categoriesSignal: WritableSignal<Category[]> = signal<Category[]>([]);
  categories$=this.categoriesSignal.asReadonly();
  isLoading$=signal<boolean>(false)
    constructor(private http: HttpClient) {}
  
    // Create a new category
    createCategory(category: Category): Observable<Category> {
      return this.http.post<Category>(`${environment.apiUrl}/categories`, category);
    }
  
    // Delete all categories
    deleteAllCategories(): Observable<Response> {
      return this.http.delete<Response>(`${environment.apiUrl}/categories`);
    }
  
    // Delete a category by ID
    deleteCategory(id: number): Observable<Response> {
      return this.http.delete<Response>(`${environment.apiUrl}/categories/${id}`);
    }
  
    // Get all categories
    getAllCategories(): Observable<Category[]> {
      return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
    }
    getAllCategoriesSignal(): void {
      this.isLoading$.set(true);
      this.http.get<Category[]>(`${environment.apiUrl}/categories`).subscribe({
        next: categories => {
          this.categoriesSignal.set(categories);
          this.isLoading$.set(false);
        },
        error: error => {
          console.error('Error fetching categories:', error);
          this.isLoading$.set(false);
        }
      })
    }
  
    // Get a category by ID
    getCategoryById(id: number): Observable<Category> {
      return this.http.get<Category>(`${environment.apiUrl}/categories/${id}`);
    }
  
    // Update a category by ID
    updateCategory(id: number, categoryRequest: Category): Observable<Category> {
      return this.http.put<Category>(`${environment.apiUrl}/categories/${id}`, categoryRequest);
    }
  }
