import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LnHtmlSectionComponent } from './ln-html-section.component';

describe('LnHtmlSectionComponent', () => {
  let component: LnHtmlSectionComponent;
  let fixture: ComponentFixture<LnHtmlSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LnHtmlSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LnHtmlSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
