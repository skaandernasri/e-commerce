import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { ContactCreateRequestDto, ContactDto, ContactResponseDto, ContactStatusUpdateDto } from '../models/contact';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Response } from '../models/response';
import { MetaPixelService } from './meta-pixel.service';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiUrl = `${environment.apiUrl}/contacts`;
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);
  pagination=signal({
    page:1,
    size:10,
  })
  constructor(private http: HttpClient,
    private metaPixelService: MetaPixelService
  ) { }
   private get httpOptions() {
    return {
      withCredentials: true, 
      headers: new HttpHeaders({ 
        'Content-Type': 'application/json'
      })
    };
  }
    createContact(contactRequest:ContactCreateRequestDto):Observable<ContactDto>{ {
      return this.http.post<ContactDto>(this.apiUrl, contactRequest, this.httpOptions)
      .pipe(tap(() => this.metaPixelService.addLeadEvent("Contact form")));
    }
  }
    updateContact(contactId:number,contactUpdateRequest:ContactStatusUpdateDto):Observable<Response>{ {
      return this.http.put<Response>(`${this.apiUrl}/${contactId}/status`, contactUpdateRequest, this.httpOptions);
    }
  }
    getContact(page: number, size: number,type:string | null,statusContact?:string | null,refundMethod?:string | null, sort?:string | null):Observable<ContactResponseDto>{ {
      return this.http.get<ContactResponseDto>(`${this.apiUrl}?page=${page}&size=${size}&type=${type}&statusContact=${statusContact}&refundMethod=${refundMethod}&sort=${sort}`, this.httpOptions)
      .pipe(tap(response => {
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
      }),
    catchError(error => {
      console.error('Error fetching contacts:', error);
      return throwError(() => error);
    }));
    }
  }
  resetCurrentPage(): void {
  this.pagination.update((pagination) => ({...pagination, page: 1}));
}
  updateCurrentPage(page: number): void {
  this.pagination.update((pagination) => ({...pagination, page}));}
}
