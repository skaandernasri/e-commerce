import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content4SectionComponent } from './content4-section.component';

describe('Content4SectionComponent', () => {
  let component: Content4SectionComponent;
  let fixture: ComponentFixture<Content4SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content4SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content4SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
