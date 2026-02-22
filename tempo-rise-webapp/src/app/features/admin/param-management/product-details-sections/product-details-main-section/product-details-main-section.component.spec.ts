import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductDetailsMainSectionComponent } from './product-details-main-section.component';

describe('ProductDetailsMainSectionComponent', () => {
  let component: ProductDetailsMainSectionComponent;
  let fixture: ComponentFixture<ProductDetailsMainSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailsMainSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductDetailsMainSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
