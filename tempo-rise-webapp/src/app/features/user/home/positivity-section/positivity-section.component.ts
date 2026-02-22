import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
import { environment } from '../../../../../environments/environment';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import { ParamSectionResponse } from '../../../../core/models/params';
interface content1Content{
  title: string;
  subtitle: string;
  imagesAndText: ImageTextPair[];
}
interface ImageTextPair {
  index: number;
  imageUrl: string;
  title: string;
  subtitle: string;
}
@Component({
  selector: 'app-positivity',
  imports: [],
  templateUrl: './positivity-section.component.html',
  styleUrl: './positivity-section.component.css'
})
export class PositivitySectionComponent {
  @Input() activeContent4Section!: Signal<ParamSectionResponse | undefined>
  private sanitizer = inject(DomSanitizer);
imageBaseUrl = environment.imageUrlBase;
  content4Content : Signal<content1Content | null> =computed(() => {
    const content = this.activeContent4Section()!.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title,
        subtitle: parsed.subtitle,
        imagesAndText: parsed.imagesAndText
      };
    }
    return null;
  })
  safeBackground(fileName: string, imageIndex?: number,content?:string): SafeStyle {
    if(fileName){
  const fullUrl = `${this.imageBaseUrl.replace(/\/$/, '')}/${encodeURIComponent(fileName)}`;
  return this.sanitizer.bypassSecurityTrustStyle(`url("${fullUrl}")`);
  }
  else if(content && content==='content4'){
    const fullUrl = this.imageAltRecord['content4'][imageIndex!];
    return this.sanitizer.bypassSecurityTrustStyle(`url("${fullUrl}")`);
  }
  else{
    return '';
  }
  }
  imageAltRecord : Record<string, string[]> = {
  'content4': ['assets/images/home/Frame364.png', 'assets/images/home/female-reproductive-system-concept-view.png']
}
  currentImageIndex1 = 0;
  svgMovedRight = false;
  changeImage1(): void {
    this.currentImageIndex1 = (this.currentImageIndex1 + 1) % 2;
    this.svgMovedRight = !this.svgMovedRight;
    
  }
}
