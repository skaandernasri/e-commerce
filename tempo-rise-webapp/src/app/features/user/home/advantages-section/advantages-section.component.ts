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
  selector: 'app-advantages',
  imports: [],
  templateUrl: './advantages-section.component.html',
  styleUrl: './advantages-section.component.css'
})
export class AdvantagesSectionComponent  {
@Input() activeContent2Section!: Signal<ParamSectionResponse | undefined>
private sanitizer = inject(DomSanitizer);
content2Content : Signal<content1Content | null> =computed(() => {
    const content = this.activeContent2Section()?.contenuJson;
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
imageBaseUrl = environment.imageUrlBase
 safeBackground(fileName: string, imageIndex?: number,content?:string): SafeStyle {
    if(fileName){
  const fullUrl = `${this.imageBaseUrl.replace(/\/$/, '')}/${encodeURIComponent(fileName)}`;
  return this.sanitizer.bypassSecurityTrustStyle(`url("${fullUrl}")`);
  }
  else if(content==='content2'){
    const fullUrl = this.imageAltRecord['content2'][imageIndex!];
    return this.sanitizer.bypassSecurityTrustStyle(`url("${fullUrl}")`);
  }
  else{
    return '';  
  }
  }
  imageAltRecord : Record<string, string[]> = {
  'content2':['assets/images/home/NewProject1.png', 'assets/images/home/front-view-woman-posing-with-green-outfit2.png',
    'assets/images/home/learn-top-organic-period-care-products-you-can-buy-with-your-fsa-ss-1925773079-web-3x21.png',
  'assets/images/home/image.png', 'assets/images/home/earth-day-environment-concept-eco-concept-1.png', 'assets/images/home/confort.png'],
}
}
