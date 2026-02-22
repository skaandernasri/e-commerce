import { Component, computed, inject, OnInit } from '@angular/core';
import { ParamsService } from '../../../core/services/params.service';
import { ParamSectionResponse } from '../../../core/models/params';
import { QuillModule } from "ngx-quill";

@Component({
  selector: 'app-about',
  imports: [QuillModule],
  templateUrl: './about.component.html',
  styleUrl: './about.component.css'
})
export class AboutComponent implements OnInit{
  private paramService= inject(ParamsService)
  activeAboutPageContent = computed(() => this.paramService.aboutPageSections().find(section => section.active))
  constructor() {}
  ngOnInit(): void {
    this.paramService.getSectionsByPageType('ABOUT').subscribe();
  }

}
