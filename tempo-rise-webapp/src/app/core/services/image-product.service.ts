import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ImageProduct } from '../models/image-product';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageProductService {
  baseUrl = environment.apiUrl;
  private apiUrl = `${this.baseUrl}/imageProduit`; // Replace with your backend API URL
  private apiUrl1=`${this.baseUrl}/imageByProductId`;
  constructor(private http: HttpClient) {}

  uploadImage(productId: number,file: File ): Observable<ImageProduct> {
    const formData = new FormData();
    formData.append('produitId', productId.toString());
    formData.append('contenu', file);
    
    // formData.append('url', file.name);
    // formData.append('productId', productId.toString());
    return this.http.post<ImageProduct>(this.apiUrl, formData);
  }
  deleteAllImages(): Observable<Response> {
    return this.http.delete<Response>(`${this.apiUrl}`);
  }

  deleteImage(id: number): Observable<Response> {
    return this.http.delete<Response>(`${this.apiUrl}/${id}`);
  }

  getAllImages(): Observable<ImageProduct[]> {
    return this.http.get<ImageProduct[]>(`${this.apiUrl}`);
  }

  getImageById(id: number): Observable<ImageProduct> {
    return this.http.get<ImageProduct>(`${this.apiUrl}/${id}`);
  }

  updateProduct(id: number, ImageProduct: ImageProduct): Observable<ImageProduct> {
    return this.http.put<ImageProduct>(`${this.apiUrl}/${id}`, ImageProduct);
  }
  getImageByProductId(id:number):Observable<ImageProduct[]>{
    return this.http.get<ImageProduct[]>(`${this.apiUrl1}/${id}`)
  }

}
