import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HtmlSectionComponent } from './about-html-section.component';

describe('HtmlSectionComponent', () => {
  let component: HtmlSectionComponent;
  let fixture: ComponentFixture<HtmlSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HtmlSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HtmlSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
