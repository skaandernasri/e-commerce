import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
import { environment } from '../../../../../environments/environment';
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
  selector: 'app-features',
  imports: [],
  templateUrl: './features-section.component.html',
  styleUrl: './features-section.component.css'
})
export class FeaturesSectionComponent  {
@Input() activeContent1Section!: Signal<ParamSectionResponse | undefined>
content1Content : Signal<content1Content | null> =computed(() => {
const content = this.activeContent1Section()?.contenuJson;
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
imageAltRecord : Record<string, string[]> = {
  'content1': ['assets/images/home/cotton.png', 'assets/images/home/fluent_drop-20-regular.png', 'assets/images/home/odor 1.png']
}
}
