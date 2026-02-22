import { Component, computed, inject, OnInit } from '@angular/core';
import { ParamsService } from '../../../core/services/params.service';
import { QuillModule } from "ngx-quill";

@Component({
  selector: 'app-tac',
  imports: [QuillModule],
  templateUrl: './tac.component.html',
  styleUrl: './tac.component.css'
})
export class TacComponent implements OnInit {
  private paramService= inject(ParamsService)
  activeTacPageContent = computed(() => this.paramService.tacPageSections().find(section => section.active))
  constructor() {}
  ngOnInit(): void {
    this.paramService.getSectionsByPageType('TERMS_AND_CONDITIONS').subscribe();
  }
}
