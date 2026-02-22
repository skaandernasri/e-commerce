import { Component, computed, inject, Input, OnInit, Signal } from '@angular/core';
import { ParamsService } from '../../../../core/services/params.service';
import { environment } from '../../../../../environments/environment';
import { CommonModule } from '@angular/common';
import { ParamSectionResponse } from '../../../../core/models/params';
interface FaqContent {
  title: string;
  subtitle: string;
  qna: FaqItem[];
}
interface FaqItem {
  question: string;
  answer: string;
  active: boolean;
}
@Component({
  selector: 'app-faq',
  imports: [CommonModule],
  templateUrl: './faq-section.component.html',
  styleUrl: './faq-section.component.css'
})
export class FaqSectionComponent {
@Input() activeFAQSection!: Signal<ParamSectionResponse | undefined>
imageBaseUrl = environment.imageUrlBase;
activeFAQ: number | null = null;
faqContent:Signal<FaqContent | null> =computed(() => {
    const content = this.activeFAQSection()?.contenuJson;
    const parsed = content ? JSON.parse(content) : null;
    if (parsed) {
      return {
        title: parsed.title,
        subtitle: parsed.subtitle,
        qna: parsed.qna
      };
    }
    return null;
  })
    toggleFAQ(index: number): void {
    if (this.activeFAQ === index) {
      this.activeFAQ = null; // Close if clicking the same question
    } else {
      this.activeFAQ = index; // Open the clicked question
    }
  }
}
