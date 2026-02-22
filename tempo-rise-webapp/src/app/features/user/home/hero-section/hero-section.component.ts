import { Component, computed, inject, Input, input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
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
  selector: 'app-hero',
  imports: [RouterModule],
  templateUrl: './hero-section.component.html',
  styleUrl: './hero-section.component.css'
})
export class HeroSectionComponent  {
@Input() activeHeroSection!: Signal<ParamSectionResponse | undefined>
imageBaseUrl = environment.imageUrlBase
heroContent:Signal<HeroContent | null> =computed(() => {
      const content = this.activeHeroSection()!.contenuJson;
      const parsed = content ? JSON.parse(content) : null;
      if (parsed) {
        return {
          title: parsed.title,
          subtitle: parsed.subtitle,
          buttonText: parsed.buttonText,
          buttonLink: parsed.buttonLink
        };
      }
      return null;
  })
  imageAltRecord : Record<string, string[]> = {
  'hero': ['assets/images/home/image.png']
}
}
