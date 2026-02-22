import { Component, effect, EventEmitter, Input, OnInit, Output, Signal } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { forkJoin, of, switchMap } from 'rxjs';
import { ParamsService } from '../../../../../core/services/params.service';
import { SwalService } from '../../../../../core/services/swal-service.service';
import { UploadImagesService } from '../../../../../core/services/upload-images.service';
import { environment } from '../../../../../../environments/environment';
import { 
  FooterGroup, 
  GET_PAGE_TYPE_SECTIONS, 
  ParamSectionResponse, 
  SocialLink, 
  TYPE_PAGE_VALUES, 
  TypePageDto 
} from '../../../../../core/models/params';
import { UploadImageComponent } from "../../../../../shared/components/upload-image/upload-image.component";

@Component({
  selector: 'app-footer-param',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, UploadImageComponent],
  templateUrl: './footer-param.component.html',
  styleUrl: './footer-param.component.css'
})
export class FooterParamComponent implements OnInit {
  @Input() selectedSection!: Signal<ParamSectionResponse | null>;
  @Output() loadPages = new EventEmitter<void>();
  
  contentForm!: FormGroup;
  imagesBaseUrl = environment.imageUrlBase;
  pageTypes = TYPE_PAGE_VALUES.filter((page) => page !== "ALL" && page !== "PRODUCT_DETAILS");

  // Page sections mapping
  pageSections = GET_PAGE_TYPE_SECTIONS;

  // Limits
  readonly limitSocials = 5;
  readonly limitGroups = 5;
  readonly limitGroupItems = 5;

  // File handling
  selectedFile: File | null = null;
  selectedItemFiles= new Map<[number, number], File>();
  selectedFiles = new Map<number, File>();
  imagePreview = '';
  imagePreviews = new Map<number, string>();
  itemsImagePreviews = new Map<[number, number], string>();
  filesToRemove: string[] = [];

  constructor(
    private fb: FormBuilder,
    private paramService: ParamsService,
    private imageUploadService: UploadImagesService,
    private toastr: SwalService
  ) {
    effect(() => {
      const section = this.selectedSection();
      if (section) {
        this.loadSectionContent(section);
      }
    });
  }

  ngOnInit(): void {
    this.initContentForm();
    const section = this.selectedSection();
    if (section) {
      this.loadSectionContent(section);
    }
  }

  // ==================== FORM INITIALIZATION ====================

  private initContentForm(): void {
    this.contentForm = this.createContentForm();
  }

  private createContentForm(): FormGroup {
    return this.fb.group({
      logoUrl: [''],
      socials: this.fb.array([]),
      groups: this.fb.array([]),
      copyright: ['']
    });
  }

  private loadSectionContent(section: ParamSectionResponse): void {
    if (!section?.contenuJson) {
      this.contentForm = this.createContentForm();
      return;
    }

    try {
      const content = JSON.parse(section.contenuJson);
      this.clearFormArrays();
      this.initializeGroups(content.groups || []);
      this.initializeSocials(content.socials || []);

      this.contentForm.patchValue({
        logoUrl: content.logoUrl || '',
        copyright: content.copyright || ''
      });
    } catch (error) {
      console.error('Error parsing contenuJson:', error);
      this.contentForm = this.createContentForm();
    }
  }

  private clearFormArrays(): void {
    while (this.footerGroupArray.length !== 0) {
      this.footerGroupArray.removeAt(0);
    }
    while (this.footerSocialLinkArray.length !== 0) {
      this.footerSocialLinkArray.removeAt(0);
    }
  }

  private initializeGroups(groups: FooterGroup[] = []): void {
    groups.forEach((group) => {
      const groupFormGroup = this.fb.group({
        index: [group.index || this.generateUid()],
        groupHeaderText: [group.groupHeaderText || ''],
        textAndLinks: this.fb.array([])
      });

      this.footerGroupArray.push(groupFormGroup);
      const groupIndex = this.footerGroupArray.length - 1;

      (group.textAndLinks || []).forEach((item) => {
        const textLinkGroup = this.fb.group({
          index: [item.index || this.generateUid()],
          linkType: [item.linkType || ''],
          selectedSection: [item.selectedSection || ''],
          selectedPage: [item.selectedPage || ''],
          text: [item.text || '', Validators.required],
          link: [item.link || ''],
          isLink: [item.isLink || false],
          imageUrl: [item.imageUrl || '']
        }, {
          validators: [this.validateLink()]
        });
        this.footerTextAndLinkArray(groupIndex).push(textLinkGroup);
      });
    });
  }

  private initializeSocials(socials: SocialLink[] = []): void {
    socials.forEach((social) => {
      const socialGroup = this.fb.group({
        index: [social.index || this.generateUid()],
        url: [social.url || ''],
        link: [social.link || ''],
        alt: [social.alt || 'facebook']
      });
      this.footerSocialLinkArray.push(socialGroup);
    });
  }

  // ==================== FORM ARRAY GETTERS ====================

  get footerGroupArray(): FormArray {
    return this.contentForm.get('groups') as FormArray;
  }

  get footerSocialLinkArray(): FormArray {
    return this.contentForm.get('socials') as FormArray;
  }

  footerTextAndLinkArray(groupIndex: number): FormArray {
    return this.footerGroupArray.at(groupIndex).get('textAndLinks') as FormArray;
  }

  // ==================== SOCIAL LINKS CRUD ====================

  addSocial(): void {
    if (this.footerSocialLinkArray.length >= this.limitSocials) return;
    this.footerSocialLinkArray.push(this.createSocialLinkGroup());
  }

  removeSocial(index: number): void {
    const imageUrl = this.footerSocialLinkArray.value[index]?.url;
    if (imageUrl) {
      this.imageUploadService.deleteImage(imageUrl).subscribe();
    }
    this.footerSocialLinkArray.removeAt(index);
    this.clearSocialImage(index);
  }

  private createSocialLinkGroup(): FormGroup {
    return this.fb.group({
      index: [this.generateUid()],
      url: [''],
      link: [''],
      alt: ['facebook']
    });
  }

  private clearSocialImage(index: number): void {
    this.selectedFiles.delete(index);
    this.imagePreviews.delete(index);
  }

  // ==================== GROUPS CRUD ====================

  addGroup(): void {
    if (this.footerGroupArray.length >= this.limitGroups) return;
    this.footerGroupArray.push(this.createGroupFormGroup());
  }

  removeGroup(index: number): void {
    this.footerGroupArray.removeAt(index);
  }

  private createGroupFormGroup(): FormGroup {
    return this.fb.group({
      index: [this.generateUid()],
      groupHeaderText: [''],
      textAndLinks: this.fb.array([])
    });
  }

  // ==================== GROUP TEXT & LINKS CRUD ====================

  addFooterGroupTextAndLinks(groupIndex: number): void {
    const textAndLinksArray = this.footerTextAndLinkArray(groupIndex);
    if (textAndLinksArray.length >= this.limitGroupItems) return;
    textAndLinksArray.push(this.createTextLinkGroup());
  }

  removeGroupTextAndLinks(groupIndex: number, itemIndex: number): void {
    this.footerTextAndLinkArray(groupIndex).removeAt(itemIndex);
  }

  private createTextLinkGroup(): FormGroup {
    return this.fb.group({
      index: [this.generateUid()],
      linkType: [''],
      selectedPage: [''],
      selectedSection: [''],
      text: ['', Validators.required],
      link: [''],
      isLink: [false],
      imageUrl: ['']
    }, {
      validators: [this.validateLink()]
    });
  }

  // ==================== LINK TYPE AND PAGE SELECTION ====================

  isSectionLink(groupIndex: number, itemIndex: number): boolean {
    const value = this.footerTextAndLinkArray(groupIndex).at(itemIndex).get('linkType')?.value;
    return value === 'SECTION_LINK';
  }

  isExternalLink(groupIndex: number, itemIndex: number): boolean {
    const value = this.footerTextAndLinkArray(groupIndex).at(itemIndex).get('linkType')?.value;
    return value === 'EXTERNAL_LINK';
  }

  isLink(groupIndex: number, itemIndex: number): boolean {
    return this.footerTextAndLinkArray(groupIndex).at(itemIndex).get('isLink')?.value === true;
  }

  onIsLinkChange(event: Event, groupIndex: number, itemIndex: number): void {
    const checkbox = event.target as HTMLInputElement;
    const formGroup = this.footerTextAndLinkArray(groupIndex).at(itemIndex);
    formGroup.patchValue({ isLink: checkbox.checked });
    
    // Reset link fields when unchecking
    if (!checkbox.checked) {
      formGroup.patchValue({
        linkType: '',
        selectedPage: '',
        selectedSection: '',
        link: ''
      });
    }
  }

  onLinkTypeChange(groupIndex: number, itemIndex: number): void {
    const formGroup = this.footerTextAndLinkArray(groupIndex).at(itemIndex);
    const linkType = formGroup.get('linkType')?.value;

    formGroup.patchValue({ 
      link: '',
      selectedPage: '',
      selectedSection: ''
    });

    if (linkType === 'PAGE_LINK') {
      const selectedPage = formGroup.get('selectedPage')?.value;
      if (selectedPage) {
        formGroup.patchValue({ link: this.getPageLinkValue(selectedPage) });
      }
    }
  }

  onPageChange(groupIndex: number, itemIndex: number, event: Event): void {
    const selectedPage = (event.target as HTMLSelectElement).value as TypePageDto;
    const formGroup = this.footerTextAndLinkArray(groupIndex).at(itemIndex);
    const linkType = formGroup.get('linkType')?.value;

    formGroup.patchValue({ 
      selectedPage,
      link: '',
      selectedSection: ''
    });

    if (linkType === 'PAGE_LINK' && selectedPage) {
      formGroup.patchValue({ link: this.getPageLinkValue(selectedPage) });
    }
  }

  getPageSections(groupIndex: number, itemIndex: number): string[] {
    const formGroup = this.footerTextAndLinkArray(groupIndex).at(itemIndex);
    const selectedPage = formGroup.get('selectedPage')?.value;
    return selectedPage ? (GET_PAGE_TYPE_SECTIONS(selectedPage) || []) : [];
  }

  onSectionChange(groupIndex: number, itemIndex: number, event: Event): void {
    const sectionName = (event.target as HTMLSelectElement).value;
    const formGroup = this.footerTextAndLinkArray(groupIndex).at(itemIndex);
    const selectedPage = formGroup.get('selectedPage')?.value;

    if (selectedPage && sectionName) {
      const link = `${this.getPageLinkValue(selectedPage)}#${sectionName}`;
      formGroup.patchValue({ link, selectedSection: sectionName });
    }
  }

  getPageLinkValue(page: TypePageDto): string {
    const pageLinks: { [key: string]: string } = {
      'ABOUT': '/about',
      'BLOG': '/blog',
      'CONTACT': '/contact',
      'HOME': '/',
      'PRODUCT': '/products',
      'TERMS_AND_CONDITIONS': '/terms-and-conditions',
      'LEGAL_NOTICE': '/legal-notice',
      'DELIVERY_AND_RETURN': '/delivery-and-return'
    };
    return pageLinks[page] || '';
  }

  // ==================== FILE HANDLING ====================

  onFileSelected(file: File): void {
    if (!file) return;
    this.selectedFile = file;
    this.imagePreview = URL.createObjectURL(file);
    if(this.contentForm.get('logoUrl')?.value)
      this.filesToRemove.push(this.contentForm.get('logoUrl')?.value);
  }

  onFileEventSocials(file: File, index: number): void {
    if (!file) return;
    this.selectedFiles.set(index, file);
    this.imagePreviews.set(index, URL.createObjectURL(file));
    if(this.footerSocialLinkArray.at(index).get('url')?.value)
      this.filesToRemove.push(this.footerSocialLinkArray.at(index).get('url')?.value);
  }

  onFileItemSelected(file: File, groupIndex: number, index: number): void {
    if (!file) return;
    this.selectedItemFiles.set([groupIndex,index], file);
    this.itemsImagePreviews.set([groupIndex,index], URL.createObjectURL(file));
    if(this.footerTextAndLinkArray(groupIndex).at(index).get('imageUrl')?.value)
      this.filesToRemove.push(this.footerTextAndLinkArray(groupIndex).at(index).get('imageUrl')?.value);
  }
  removeFile(): void {
    if(this.contentForm.get('logoUrl')?.value)
      this.filesToRemove.push(this.contentForm.get('logoUrl')?.value);
    this.selectedFile = null;
    this.imagePreview = '';
    this.contentForm.patchValue({ logoUrl: '' });
  }

  removeFileSocials(index: number): void {
    if(this.footerSocialLinkArray.at(index).get('url')?.value)
      this.filesToRemove.push(this.footerSocialLinkArray.at(index).get('url')?.value);
    this.selectedFiles.delete(index);
    this.imagePreviews.delete(index);
    this.footerSocialLinkArray.at(index).patchValue({ url: '' });
  }

  removeFileItem(groupIndex: number,index: number): void {
    if(this.footerTextAndLinkArray(groupIndex).at(index).get('imageUrl')?.value)
      this.filesToRemove.push(this.footerTextAndLinkArray(groupIndex).at(index).get('imageUrl')?.value);
    this.selectedItemFiles.delete([groupIndex,index]);
    this.itemsImagePreviews.delete([groupIndex,index]);
    this.footerTextAndLinkArray(groupIndex).at(index).patchValue({ imageUrl: '' });
  }

  deleteOldImage(){
    if(this.filesToRemove.length > 0)
      this.filesToRemove.forEach(imageUrl => this.imageUploadService.deleteImage(imageUrl).subscribe({
        error: (error) => console.error('Error deleting image:', error)
        }));
    this.filesToRemove = [];
  }
  // ==================== SAVE CONTENT ====================

  saveContent(): void {
    if (!this.contentForm.valid) {
      this.toastr.error('Veuillez remplir tous les champs requis');
      return;
    }

    const section = this.selectedSection();
    if (!section?.id) {
      this.toastr.error('Section non trouvée');
      return;
    }

    this.clearContentBeforeSave();
    const hasImages = this.selectedFiles.size > 0 || this.selectedFile || this.selectedItemFiles.size > 0;

    if (hasImages) {
      this.saveContentWithImages(section);
    } else {
      this.saveContentStandard(section);
    }
  }

  private saveContentWithImages(section: ParamSectionResponse): void {
    const uploadObservables = [
      ...this.createSocialImageUploads(),
      ...this.createItemsImageUploads(),
      this.createLogoUpload()
    ].filter(obs => obs !== null);

    forkJoin(uploadObservables).pipe(
      switchMap(() => this.updateSection(section))
    ).subscribe({
      next: () => this.handleSaveSuccess(),
      error: (error) => this.handleSaveError(error)
    });
  }

  private saveContentStandard(section: ParamSectionResponse): void {
    this.updateSection(section).subscribe({
      next: () => this.handleSaveSuccess(),
      error: (error) => this.handleSaveError(error)
    });
  }

  private createSocialImageUploads() {
    return Array.from(this.selectedFiles).map(([imageIndex, file]) => {
      return this.imageUploadService.uploadImage(file).pipe(
        switchMap((response: any) => {
          this.updateSocialImageUrl(imageIndex, response.filename);
          this.toastr.success(`Image ${imageIndex + 1} chargée avec succès`);
          return of(response);
        })
      );
    });
  }

  private createItemsImageUploads() {
    return Array.from(this.selectedItemFiles).map(([[groupIndex, itemIndex], file]) => {
      return this.imageUploadService.uploadImage(file).pipe(
        switchMap((response: any) => {
          this.updateItemImageUrl(groupIndex,itemIndex, response.filename);
          this.toastr.success(`Image ${itemIndex + 1} chargée avec succès`);
          return of(response);
        })
      )
    })
  }


  private createLogoUpload() {
    if (!this.selectedFile) return null;

    return this.imageUploadService.uploadImage(this.selectedFile).pipe(
      switchMap((response: any) => {
        this.contentForm.patchValue({ logoUrl: response.filename });
        this.toastr.success('Logo chargé avec succès');
        return of(response);
      })
    );
  }

  private updateSocialImageUrl(imageIndex: number, filename: string): void {
    const socialItem = this.footerSocialLinkArray.at(imageIndex);
    if (socialItem) {
      socialItem.patchValue({ url: filename });
    }
  }

  private updateItemImageUrl(groupIndex: number,itemIndex: number, filename: string): void {
    const item = this.footerTextAndLinkArray(groupIndex).at(itemIndex);
    if (item) {
      item.patchValue({ imageUrl: filename });
    }
  }

  private updateSection(section: ParamSectionResponse) {
    const contenuJson = JSON.stringify(this.contentForm.value);
    return this.paramService.updateSection(section.id!, {
      ...section,
      contenuJson
    });
  }

  private handleSaveSuccess(): void {
    this.deleteOldImage();
    this.toastr.success('Contenu mis à jour avec succès');
    this.selectedFile = null;
    this.selectedFiles.clear();
    this.imagePreview = '';
    this.imagePreviews.clear();
    this.loadPages.emit();
  }

  private handleSaveError(error: any): void {
    console.error('Error during save process:', error);
    this.toastr.error('Erreur lors de la mise à jour');
  }

  private clearContentBeforeSave(): void {
    const groups = this.contentForm.get('groups') as FormArray;
    groups.controls.forEach((groupCtrl, groupIndex) => {
      const group = groupCtrl as FormGroup;
      const textAndLinks = group.get('textAndLinks') as FormArray;
      textAndLinks.controls.forEach((itemCtrl, itemIndex) => {
        const item = itemCtrl as FormGroup;
        if (!this.isLink(groupIndex, itemIndex)) {
          item.patchValue({
            link: '',
            selectedPage: '',
            selectedSection: '',
            linkType: '',
            isLink: false
          });
        }
      });
    });
  }

  private validateLink(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const isLink = group.get('isLink')?.value;
      const linkType = group.get('linkType')?.value;
      const link = group.get('link')?.value;

      if (!isLink) return null;

      if (!linkType) {
        return { linkTypeRequired: true };
      }

      if (linkType === 'EXTERNAL_LINK' && !link) {
        return { externalLinkRequired: true };
      }

      if (linkType === 'PAGE_LINK') {
        const selectedPage = group.get('selectedPage')?.value;
        if (!selectedPage) {
          return { pageRequired: true };
        }
      }

      if (linkType === 'SECTION_LINK') {
        const selectedPage = group.get('selectedPage')?.value;
        const selectedSection = group.get('selectedSection')?.value;
        if (!selectedPage || !selectedSection) {
          return { sectionRequired: true };
        }
      }

      return null;
    };
  }

  private generateUid(): string {
    return crypto.randomUUID();
  }
}