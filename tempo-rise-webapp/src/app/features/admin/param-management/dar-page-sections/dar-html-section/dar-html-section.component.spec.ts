import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DarHtmlSectionComponent } from './dar-html-section.component';

describe('DarHtmlSectionComponent', () => {
  let component: DarHtmlSectionComponent;
  let fixture: ComponentFixture<DarHtmlSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DarHtmlSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DarHtmlSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
