import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content2SectionComponent } from './content2-section.component';

describe('Content2SectionComponent', () => {
  let component: Content2SectionComponent;
  let fixture: ComponentFixture<Content2SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content2SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content2SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
