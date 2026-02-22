import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { UploadImageResponse } from '../models/upload';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UploadImagesService {

  constructor(private http: HttpClient) { }
  private baseUrl = `${environment.apiUrl}/upload`
    private get httpOptions() {
    return {
      withCredentials: true, 
    };
}
  uploadImage(file: File): Observable<UploadImageResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}`, formData, this.httpOptions);
  }
  deleteImage(filename: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${filename}`, this.httpOptions);
  }
}
