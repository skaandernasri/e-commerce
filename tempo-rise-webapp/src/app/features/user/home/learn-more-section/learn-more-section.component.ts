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
  selector: 'app-learn-more',
  imports: [],
  templateUrl: './learn-more-section.component.html',
  styleUrl: './learn-more-section.component.css'
})
export class LearnMoreSectionComponent  {
@Input() activeContent7Section!: Signal<ParamSectionResponse | undefined>
imageBaseUrl = environment.imageUrlBase;
  content7Content : Signal<content1Content | null> =computed(() => {
    const content = this.activeContent7Section()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title,
        subtitle: '',
        imagesAndText: parsed.imagesAndText
      };
    }
    return null;
  })
  imageAltRecord : Record<string, string[]> = {
  'content7':['assets/images/home/info1.png', 'assets/images/home/info2.png', 'assets/images/home/info3.png']
}
}
