import { HttpClient } from '@angular/common/http';
import { Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { map, Observable, tap } from 'rxjs';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private apiUrl = environment.apiUrl +'/utilisateurs';
  private users:WritableSignal<User[]> = signal<User[]>([])
  users$ = this.users.asReadonly()
  constructor(private http: HttpClient) {}

  // Create a new product
  // createProduct(Product: Product): Observable<Product> {
  //   return this.http.post<Product>(`${this.apiUrl}`, Product);
  // }
  createUser(userRequest: any): Observable<User> {
    // Transform the data to match backend expectations
    return this.http.post<User>(`${this.apiUrl}`, userRequest).pipe(
      tap((user: User) => {
        this.users.update(users => [...users, user]);
      })
    );
  }
  // Delete all products
  deleteAllUsers(): Observable<Response> {
    return this.http.delete<Response>(`${this.apiUrl}`).pipe(
      tap(() => {
        this.users.set([]);
      })
    );
  }

  // Delete a product by ID
  deleteUser(id: number): Observable<Response> {
    return this.http.delete<Response>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        this.users.update(users => users.filter(user => user.id !== id));
      })
    );
  }

  // Get all products
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}`).pipe(
      tap(users => {
        this.users.set(users);
      })
    );
  }

  // Get a product by ID
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  // Update a product by ID
  // updateProduct(id: number, productRequest: Product): Observable<Product> {
  //   return this.http.put<Product>(`${this.apiUrl}/${id}`, productRequest);
  // }
  updateUser(id: number, userRequest: any): Observable<User> {
  
    return this.http.put<User>(`${this.apiUrl}/${id}`, userRequest).pipe(
      tap(updatedUser => {
        this.users.update(users => 
          users.map(user => user.id === id ? { ...user, ...updatedUser } : user)
        );
      })
    );
  }
  updateUserProfile( userRequest: any): Observable<User> {
  
    return this.http.put<User>(`${environment.apiUrl}/utilisateur/profile`, userRequest);
  }
  updateUserProfileImage( userId: number,image: FormData): Observable<any> {
    return this.http.post(`${environment.apiUrl}/utilisateur/${userId}/profile/image`, image);
  }
}
