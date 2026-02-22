import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OurReviewsSectionComponent } from './our-reviews-section.component';

describe('OurReviewsSectionComponent', () => {
  let component: OurReviewsSectionComponent;
  let fixture: ComponentFixture<OurReviewsSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OurReviewsSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OurReviewsSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
