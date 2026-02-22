import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
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
  selector: 'app-new-letter',
  imports: [RouterModule],
  templateUrl: './new-letter-section.component.html',
  styleUrl: './new-letter-section.component.css'
})
export class NewLetterSectionComponent {
@Input() activeContent8Section!: Signal<ParamSectionResponse | undefined>
imageBaseUrl = environment.imageUrlBase;
  content8Content : Signal<HeroContent | null> =computed(() => {
    const content = this.activeContent8Section()!.contenuJson;
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
  });
}
