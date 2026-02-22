import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content6SectionComponent } from './content6-section.component';

describe('Content6SectionComponent', () => {
  let component: Content6SectionComponent;
  let fixture: ComponentFixture<Content6SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content6SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content6SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
