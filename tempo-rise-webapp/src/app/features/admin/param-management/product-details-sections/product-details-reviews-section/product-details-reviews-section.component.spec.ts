import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductDetailsReviewsSectionComponent } from './product-details-reviews-section.component';

describe('ProductDetailsReviewsSectionComponent', () => {
  let component: ProductDetailsReviewsSectionComponent;
  let fixture: ComponentFixture<ProductDetailsReviewsSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailsReviewsSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductDetailsReviewsSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
