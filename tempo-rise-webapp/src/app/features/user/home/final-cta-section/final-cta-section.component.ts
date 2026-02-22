import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from '../../../../../environments/environment';
import { RouterModule } from '@angular/router';
import { ParamSectionResponse } from '../../../../core/models/params';
interface HeroContent {
  title: string;
  subtitle: string;
  buttonText: string;
  buttonLink: string;
}
@Component({
  selector: 'app-final-cta',
  imports: [RouterModule],
  templateUrl: './final-cta-section.component.html',
  styleUrl: './final-cta-section.component.css'
})
export class FinalCtaSectionComponent  {
@Input() activeContent6Section!: Signal<ParamSectionResponse | undefined>
imageBaseUrl = environment.imageUrlBase;
  content6Content : Signal<HeroContent | null> =computed(() => {
    const content = this.activeContent6Section()!.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title,
          subtitle: '',
          buttonText: parsed.buttonText,
          buttonLink: parsed.buttonLink
      };
    }
    return null;
  })
  imageAltRecord : Record<string, string[]> = {
  'content6': ['assets/images/home/unsplash_LiDZooBvzt0.png']
}
}
