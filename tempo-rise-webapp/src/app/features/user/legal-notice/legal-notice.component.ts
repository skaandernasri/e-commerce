import { Component, computed, inject, OnInit } from '@angular/core';
import { ParamsService } from '../../../core/services/params.service';
import { QuillModule } from "ngx-quill";

@Component({
  selector: 'app-legal-notice',
  imports: [QuillModule],
  templateUrl: './legal-notice.component.html',
  styleUrl: './legal-notice.component.css'
})
export class LegalNoticeComponent implements OnInit {
  private paramService= inject(ParamsService)
  activeLegalNoticePageContent = computed(() => this.paramService.legalNoticePageSections().find(section => section.active))
  constructor() {}
  ngOnInit(): void {
    this.paramService.getSectionsByPageType('LEGAL_NOTICE').subscribe();
  }
}
