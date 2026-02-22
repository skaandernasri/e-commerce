import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
import { environment } from '../../../../../environments/environment';
import { ParamSectionResponse } from '../../../../core/models/params';

@Component({
  selector: 'app-how-it-works',
  imports: [],
  templateUrl: './how-it-works-section.component.html',
  styleUrl: './how-it-works-section.component.css'
})
export class HowItWorksSectionComponent  {
@Input() activeContent3Section!: Signal<ParamSectionResponse | undefined>
imageBaseUrl = environment.imageUrlBase;
imageAltRecord : Record<string, string[]> = {
  'content3': ['assets/images/home/Frame347(fr).png']
}
}
