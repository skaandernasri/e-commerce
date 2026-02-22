// admin-pages.component.ts
import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SwalService } from '../../../core/services/swal-service.service';
import { ParamsService } from '../../../core/services/params.service';
import { environment } from '../../../../environments/environment';
import { FooterParamComponent } from "./All-page-sections/footer-param/footer-param.component";
import { HeroSectionComponent } from "./landing-page-sections/hero-section/hero-section.component";
import { Content1SectionComponent } from "./landing-page-sections/content1-section/content1-section.component";
import { ABOUT_PAGE_SECTIONS, ALL_PAGE_SECTIONS, BLOG_PAGE_SECTIONS, CONTACT_PAGE_SECTIONS,
   DAR_PAGE_SECTIONS, GET_PAGE_TYPE_ICON, GET_PAGE_TYPE_LABEL, GET_SECTION_TYPE_LABEL,
    HOME_PAGE_SECTIONS, LEGAL_NOTICE_PAGE_SECTIONS,
     ParamSectionResponse, PRODUCT_DETAILS_PAGE_SECTIONS, PRODUCTS_PAGE_SECTIONS,
      TAC_PAGE_SECTIONS, TYPE_PAGE_VALUES, TypePageDto } from '../../../core/models/params';
import { Content2SectionComponent } from "./landing-page-sections/content2-section/content2-section.component";
import { OurProductsSectionComponent } from "./landing-page-sections/our-products-section/our-products-section.component";
import { Content4SectionComponent } from "./landing-page-sections/content4-section/content4-section.component";
import { Content5SectionComponent } from "./landing-page-sections/content5-section/content5-section.component";
import { Content6SectionComponent } from "./landing-page-sections/content6-section/content6-section.component";
import { Content7SectionComponent } from "./landing-page-sections/content7-section/content7-section.component";
import { OurReviewsSectionComponent } from "./landing-page-sections/our-reviews-section/our-reviews-section.component";
import { FaqSectionComponent } from "./landing-page-sections/faq-section/faq-section.component";
import { Content8SectionComponent } from "./landing-page-sections/content8-section/content8-section.component";
import { AboutHtmlSectionComponent } from "./About-page-sections/html-section/about-html-section.component";
import { TacHtmlSectionComponent } from "./TAC-page-sections/html-section/tac-html-section.component";
import { ProductsMainSectionComponent } from "./Products-page-sections/products-main-section/products-main-section.component";
import { BlogsMainSectionComponent } from "./Blogs-page-sections/blogs-main-section/blogs-main-section.component";
import { ContactMainSectionComponent } from "./contact-page-sections/contact-main-section/contact-main-section.component";
import { DarHtmlSectionComponent } from "./dar-page-sections/dar-html-section/dar-html-section.component";
import { LnHtmlSectionComponent } from "./legal-notice-page-sections/ln-html-section/ln-html-section.component";
import { RightBannerSectionComponent } from "./All-page-sections/right-banner-section/right-banner-section.component";
import { BannerSectionComponent } from "./All-page-sections/banner-section/banner-section.component";

interface PageWithSections {
  type: TypePageDto;
  label: string;
  sections: ParamSectionResponse[];
}

@Component({
  selector: 'app-admin-pages',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule,
    FooterParamComponent, HeroSectionComponent,
    Content1SectionComponent, Content2SectionComponent,
    OurProductsSectionComponent, Content4SectionComponent,
    Content5SectionComponent, Content6SectionComponent,
    Content7SectionComponent, OurReviewsSectionComponent,
    FaqSectionComponent, Content8SectionComponent,
    TacHtmlSectionComponent, AboutHtmlSectionComponent,
    ProductsMainSectionComponent,
    BlogsMainSectionComponent,
    ContactMainSectionComponent, DarHtmlSectionComponent, LnHtmlSectionComponent, RightBannerSectionComponent, BannerSectionComponent],
  templateUrl: './param-management.component.html'
})
export class ParamManagementComponent implements OnInit {
  // Signals for state management
  pages = signal<PageWithSections[]>([]);
  selectedPage = signal<PageWithSections | null>(null);
  selectedSection = signal<ParamSectionResponse | null>(null);
  selectedSectionReadOnly = this.selectedSection.asReadonly();
  showSectionModal = signal(false);
  editMode = signal(false);
  isLoading = signal(false);
  // Forms
  sectionForm!: FormGroup;
  contentForm!: FormGroup;
  
  // File handling
  selectedFile: File | null = null;
  imagePreviews = '';
  content1SelectedFiles: Map<number, File> = new Map();
  content1ImagePreviews: Map<number, string> = new Map();
  
  // Environment
  imagesBaseUrl = environment.imageUrlBase;

  // Computed values
  selectedPageSections = computed(() => this.selectedPage());
  hasSelectedPage = computed(() => this.selectedPage() !== null);
  hasSelectedSection = computed(() => this.selectedSection() !== null);
  totalSections = computed(() => {
    return this.pages().reduce((sum, page) => sum + page.sections.length, 0);
  });
  
  selectedPageSectionCount = computed(() => {
    return this.selectedPage()?.sections.length || 0;
  });
  pageTypes=TYPE_PAGE_VALUES;// Create a copy of the enum values
  // Section types configuration
  homePageSections=HOME_PAGE_SECTIONS;
  allPageSections=ALL_PAGE_SECTIONS;
  aboutPageSections=ABOUT_PAGE_SECTIONS;
  TACPageSections=TAC_PAGE_SECTIONS;
  productsPageSections=PRODUCTS_PAGE_SECTIONS;
  blogPageSections=BLOG_PAGE_SECTIONS;
  productDetailsPageSections=PRODUCT_DETAILS_PAGE_SECTIONS;
  contactPageSections=CONTACT_PAGE_SECTIONS;
  deliveryAndReturnPageSections=DAR_PAGE_SECTIONS;
  legalNoticePageSections=LEGAL_NOTICE_PAGE_SECTIONS;

  constructor(
    private fb: FormBuilder,
    private paramService: ParamsService,
    private toastr: SwalService,
  ) {
  }

  ngOnInit(): void {
    this.initForms();
    this.loadPages();
  }
  handleFooterPageEmit(): void {
    this.loadPages();
  }
  // Form Initialization
  initForms(): void {
    this.sectionForm = this.fb.group({
      id: [null],
      titre: ['', Validators.required],
      type: ['CONTENT1', Validators.required],
      typePage: [{ value: null, disabled: true }, Validators.required],
      imageUrl: [''],
      active: [true]
    });

  }
  // Data Loading
  loadPages(): void {
    this.isLoading.set(true);
    
    this.paramService.getSections().subscribe({
      next: (sections) => {
        this.organizeSectionsByPage(sections);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading sections:', error);
        this.toastr.error('Erreur de chargement des pages');
        this.isLoading.set(false);
      }
    });
  }

  organizeSectionsByPage(sections: ParamSectionResponse[]): void {
    const organizedPages = this.pageTypes.map(type => ({
      type,
      label: this.getPageTypeLabel(type),
      sections: sections.filter(s => s.typePage === type)
    }));

    this.pages.set(organizedPages);
    this.updateSelectedPageAndSection(organizedPages);
  }

  updateSelectedPageAndSection(organizedPages: PageWithSections[]): void {
    const currentPage = this.selectedPage();
    if (!currentPage) return;

    const updatedPage = organizedPages.find(p => p.type === currentPage.type);
    if (updatedPage) {
      this.selectedPage.set(updatedPage);
      
      const currentSection = this.selectedSection();
      if (currentSection) {
        const updatedSection = updatedPage.sections.find(s => s.id === currentSection.id);
        if (updatedSection) {
          this.selectedSection.set(updatedSection);
        }
      }
    }
  }

  // Selection Methods
  selectPage(page: PageWithSections): void {
    this.selectedPage.set(page);
    this.selectedSection.set(null);
  }

  selectSection(section: ParamSectionResponse): void {
    this.selectedSection.set(section);
  }

  // Section CRUD Operations
  openSectionModal(section?: ParamSectionResponse): void {
    const page = this.selectedPage();
    if (!page) return;

    this.resetModalState();
    this.editMode.set(!!section);

    if (section) {
      this.sectionForm.patchValue(section);
    } else {
      this.sectionForm.reset({ 
        active: true, 
        typePage: page.type,
        type: 'CONTENT',
        imageUrl: ''
      });
    }
    
    this.showSectionModal.set(true);
  }

  resetModalState(): void {
    this.imagePreviews = '';
    this.selectedFile = null;
  }

  saveSection(): void {
    if (!this.sectionForm.valid) return;

    this.sectionForm.get('typePage')?.enable();
    const data = this.sectionForm.value;
    
    if (this.editMode()) {
      this.updateSection(data);
    } else {
      this.createSection(data);
    }
  }

  updateSection(data: any): void {
    this.paramService.getSectionById(data.id).subscribe(section => {
      const updatedData = {
        ...section,
        titre: data.titre,
        type: data.type,
        typePage: data.typePage,
        active: data.active,
        imageUrl: data.imageUrl
      };

      this.paramService.updateSection(data.id, updatedData).subscribe({
        next: () => {
          this.handleSectionUpdateSuccess(data.id);
        },
        error: (error) => {
          console.error('Error updating section:', error);
          this.toastr.error('Erreur de mise à jour');
          this.sectionForm.get('typePage')?.disable();
        }
      });
    });
  }

  handleSectionUpdateSuccess(sectionId: number): void {
    this.toastr.success('Section mise à jour avec succès');
    this.loadPages();
    this.showSectionModal.set(false);
    
    if (this.selectedFile) {
      this.uploadSectionImage(sectionId, this.selectedFile);
    } else if (this.sectionForm.value.imageUrl === '') {
      this.deleteSectionImage(sectionId);
    }
    
    this.sectionForm.get('typePage')?.disable();
  }

  createSection(data: any): void {
    this.paramService.createSection(data).subscribe({
      next: (response) => {
        if (this.selectedFile && response.id) {
          this.uploadSectionImage(response.id, this.selectedFile);
        }
        this.toastr.success('Section créée avec succès');
        this.loadPages();
        this.showSectionModal.set(false);
        this.sectionForm.get('typePage')?.disable();
      },
      error: (error) => {
        console.error('Error creating section:', error);
        this.toastr.error('Erreur de création');
        this.sectionForm.get('typePage')?.disable();
      }
    });
  }

  deleteSection(id: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette section ?')) return;

    this.paramService.deleteSection(id).subscribe({
      next: () => {
        this.toastr.success('Section supprimée avec succès');
        this.loadPages();
        this.selectedSection.set(null);
      },
      error: (error) => {
        console.error('Error deleting section:', error);
        this.toastr.error('Erreur de suppression');
      }
    });
  }

  // File Upload Handlers
  onFileEvent(event: Event): void {
    const file = this.getFileFromEvent(event);
    if (!file) return;

    this.selectedFile = file;
    this.imagePreviews = URL.createObjectURL(file);
  }


  getFileFromEvent(event: Event): File | null {
    const input = event.target as HTMLInputElement;
    return input.files && input.files.length > 0 ? input.files[0] : null;
  }

  removeFile(fromSectionForm: boolean): void {
    if (fromSectionForm) {
      this.sectionForm.patchValue({ imageUrl: '' });
    } else {
      this.selectedFile = null;
      this.imagePreviews = '';
    }
  }


  uploadSectionImage(sectionId: number, file: File): void {
    this.paramService.uploadImageSection(sectionId, file).subscribe({
      next: () => {
        this.selectedFile = null;
        this.imagePreviews = '';
        this.loadPages();
        this.toastr.success('Image chargée avec succès');
      },
      error: (error) => {
        this.selectedFile = null;
        this.imagePreviews = '';
        console.error('Error uploading image:', error);
        this.toastr.error('Erreur de chargement de l\'image');
      }
    });
  }

  deleteSectionImage(sectionId: number): void {
    this.paramService.deleteSectionImage(sectionId).subscribe({
      next: () => {
        this.loadPages();
        this.toastr.success('Image supprimée avec succès');
      },
      error: (error) => {
        console.error('Error deleting image:', error);
        this.toastr.error('Erreur de suppression de l\'image');
      }
    });
  }


  // Utility Methods
  getSectionTypeLabel = GET_SECTION_TYPE_LABEL;
  getPageTypeLabel = GET_PAGE_TYPE_LABEL;
  getPageTypeIcon = GET_PAGE_TYPE_ICON;
  closeModal(): void {
    this.resetModalState();
    this.showSectionModal.set(false);
  }

  // Placeholder methods for future implementation
  addMenuItem(): void {
    // TODO: Implementation for adding menu items
  }

  addSocialLink(): void {
    // TODO: Implementation for adding social links
  }

  addBannerItem(): void {
    // TODO: Implementation for adding banner items
  }
}