import { Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { 
  AboutPageSectionType,
  AllPageSectionType,
  BlogPageSectionType,
  HomePageSectionType,
  ParamSectionRequest, 
  ParamSectionResponse, 
  ProductDetailsPageSectionType, 
  ProductsPageSectionType, 
  TACPageSectionType, 
  TypePageDto} 
  from '../models/params';

@Injectable({
  providedIn: 'root'
})
export class ParamsService {
  private readonly apiUrl = `${environment.apiUrl}`;
  
  // Signals
  private readonly _landingPageSections = signal<ParamSectionResponse[]>([]);
  readonly landingPageSections = this._landingPageSections.asReadonly();
  
  private readonly _landingPageSectionsIsLoading = signal<boolean>(false);
  readonly landingPageSectionsIsLoading = this._landingPageSectionsIsLoading.asReadonly();
  
  private readonly _allPageSections = signal<ParamSectionResponse[]>([]);
  readonly allPageSections = this._allPageSections.asReadonly();

  private readonly _aboutPageSections = signal<ParamSectionResponse[]>([]);
  readonly aboutPageSections = this._aboutPageSections.asReadonly();

  private readonly _tacPageSections = signal<ParamSectionResponse[]>([]);
  readonly tacPageSections = this._tacPageSections.asReadonly();

  private readonly _productsPageSections = signal<ParamSectionResponse[]>([]);
  readonly productsPageSections = this._productsPageSections.asReadonly();

  private readonly _blogsPageSections = signal<ParamSectionResponse[]>([]);
  readonly blogsPageSections = this._blogsPageSections.asReadonly();

  private readonly _productDetailsPageSections = signal<ParamSectionResponse[]>([]);
  readonly productDetailsPageSections = this._productDetailsPageSections.asReadonly();

  private readonly _contactPageSections = signal<ParamSectionResponse[]>([]);
  readonly contactPageSections = this._contactPageSections.asReadonly();

  private readonly _darPageSections = signal<ParamSectionResponse[]>([]);
  readonly darPageSections = this._darPageSections.asReadonly();

  private readonly _legalNoticePageSections = signal<ParamSectionResponse[]>([]);
  readonly legalNoticePageSections = this._legalNoticePageSections.asReadonly();

  constructor(private http: HttpClient) {}

  // ========== SECTION CRUD ==========
  
  getSectionsByType(type: HomePageSectionType |
     AllPageSectionType | AboutPageSectionType |
      TACPageSectionType | ProductsPageSectionType |
       BlogPageSectionType | ProductDetailsPageSectionType): Observable<ParamSectionResponse[]> {
    return this.http.get<ParamSectionResponse[]>(`${this.apiUrl}/section/type/${type}`).pipe(
      tap(sections => this.addOrUpdateSections(sections))
    );
  }

  getSectionsByPageType(type: TypePageDto): Observable<ParamSectionResponse[]> {
    return this.http.get<ParamSectionResponse[]>(`${this.apiUrl}/section/pageType/${type}`).pipe(
      tap(sections => this.addOrUpdateSections(sections))
    );
  }

  getSections(): Observable<ParamSectionResponse[]> {
    return this.http.get<ParamSectionResponse[]>(`${this.apiUrl}/section`).pipe(
      tap(sections => this.addOrUpdateSections(sections))
    );
  }

  getSectionById(id: number): Observable<ParamSectionResponse> {
    return this.http.get<ParamSectionResponse>(`${this.apiUrl}/section/${id}`).pipe(
      tap(section => this.addOrUpdateSection(section))
    );
  }

  createSection(section: ParamSectionRequest): Observable<ParamSectionResponse> {
    return this.http.post<ParamSectionResponse>(`${this.apiUrl}/section`, section).pipe(
      tap(section => this.addOrUpdateSection(section))
    );
  }

  updateSection(id: number, section: ParamSectionRequest): Observable<ParamSectionResponse> {
    return this.http.put<ParamSectionResponse>(`${this.apiUrl}/section/${id}`, section).pipe(
      tap(section => this.addOrUpdateSection(section))
    );
  }

  deleteSection(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/section/${id}`).pipe(
      tap(() => this.removeSectionFromSignals(id))
    );
  }

  deleteSectionImage(sectionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/section/image/${sectionId}`).pipe(
      tap(() => this.getSectionById(sectionId).subscribe())
    );
  }

  uploadImageSection(sectionId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('id', sectionId.toString());
    formData.append('contenu', file);
    return this.http.post<any>(`${this.apiUrl}/section/image`, formData).pipe(
      tap(() => this.getSectionById(sectionId).subscribe())
    );
  }
  // ========== UTILITY METHODS ==========
  
  reorderSections(pageId: number, sectionIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/sections/reorder`, { pageId, sectionIds });
  }

  reorderGroups(sectionId: number, groupIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/groups/reorder`, { sectionId, groupIds });
  }

  reorderItems(parentId: number, itemIds: number[], parentType: 'section' | 'group'): Observable<void> {
    const endpoint = parentType === 'section' 
      ? `${this.apiUrl}/items/reorder/section`
      : `${this.apiUrl}/items/reorder/group`;
    return this.http.put<void>(endpoint, { parentId, itemIds });
  }

  // ========== SIGNAL MANAGEMENT ==========

  private addOrUpdateSection(section: ParamSectionResponse): void {
    // Update all pages sections
    switch (section.typePage) {
      case 'ALL':
        this.updateSignal(this._allPageSections, section);
        break;
      case 'HOME':
        this.updateSignal(this._landingPageSections, section);
        break;
      case 'ABOUT':
        this.updateSignal(this._aboutPageSections, section);
        break;
      case 'TERMS_AND_CONDITIONS':
        this.updateSignal(this._tacPageSections, section);
        break;
      case 'PRODUCT':
        this.updateSignal(this._productsPageSections, section);
        break;
      case 'BLOG':
        this.updateSignal(this._blogsPageSections, section);
        break;
      case 'PRODUCT_DETAILS':
        this.updateSignal(this._productDetailsPageSections, section);
        break;
      case 'CONTACT':
        this.updateSignal(this._contactPageSections, section);
        break;
      case 'DELIVERY_AND_RETURN':
        this.updateSignal(this._darPageSections, section);
        break;
      case 'LEGAL_NOTICE':
        this.updateSignal(this._legalNoticePageSections, section);
        break;
    }
  }

  private addOrUpdateSections(sections: ParamSectionResponse[]): void {
    sections.forEach(section => this.addOrUpdateSection(section));
  }

  private updateSignal(
    signal: WritableSignal<ParamSectionResponse[]>, 
    section: ParamSectionResponse
  ): void {
    signal.update(sections => {
      const existingIndex = sections.findIndex(s => s.id === section.id);
      
      if (existingIndex !== -1) {
        // Update existing section
        const updated = [...sections];
        updated[existingIndex] = section;
        
        return updated;
      }
      
      // Add new section
      
      return [...sections, section];
    });
  }

  private removeSectionFromSignals(id: number): void {
    this._allPageSections.update(sections => 
      sections.filter(section => section.id !== id)
    );
    
    this._landingPageSections.update(sections => 
      sections.filter(section => section.id !== id)
    );
  }
}