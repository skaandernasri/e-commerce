import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content8SectionComponent } from './content8-section.component';

describe('Content8SectionComponent', () => {
  let component: Content8SectionComponent;
  let fixture: ComponentFixture<Content8SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content8SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content8SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
