import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductDetailsDescriptionSectionComponent } from './product-details-description-section.component';

describe('ProductDetailsDescriptionSectionComponent', () => {
  let component: ProductDetailsDescriptionSectionComponent;
  let fixture: ComponentFixture<ProductDetailsDescriptionSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailsDescriptionSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductDetailsDescriptionSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
