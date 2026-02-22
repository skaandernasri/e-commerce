import { Component, computed, inject, OnInit } from '@angular/core';
import { ParamsService } from '../../../core/services/params.service';
import { QuillModule } from "ngx-quill";

@Component({
  selector: 'app-delivery-and-return',
  imports: [QuillModule],
  templateUrl: './delivery-and-return.component.html',
  styleUrl: './delivery-and-return.component.css'
})
export class DeliveryAndReturnComponent implements OnInit {
  private paramService= inject(ParamsService)
  activeDarPageContent = computed(() => this.paramService.darPageSections().find(section => section.active))
  constructor() {}
  ngOnInit(): void {
    this.paramService.getSectionsByPageType('DELIVERY_AND_RETURN').subscribe();
  }
}
