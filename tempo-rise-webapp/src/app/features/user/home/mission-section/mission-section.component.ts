import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from '../../../../../environments/environment';
import { ParamSectionResponse } from '../../../../core/models/params';
interface content5Content{
  title: string;
  subtitle: string;
  title1: string;
  subtitle1: string;
  title2: string;
  subtitle2: string;
  title3: string;
  subtitle3: string;
}
@Component({
  selector: 'app-mission',
  imports: [],
  templateUrl: './mission-section.component.html',
  styleUrl: './mission-section.component.css'
})
export class MissionSectionComponent {
@Input() activeContent5Section!: Signal<ParamSectionResponse | undefined>
  imageBaseUrl = environment.imageUrlBase;
    content5Content : Signal<content5Content | null> =computed(() => {
    const content = this.activeContent5Section()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title,
        subtitle: parsed.subtitle,
        title1: parsed.title1,
        subtitle1: parsed.subtitle1,
        title2: parsed.title2,
        subtitle2: parsed.subtitle2,
        title3: parsed.title3,
        subtitle3: parsed.subtitle3,
      };
    }
    return null;
  })
}
