import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductsMainSectionComponent } from './products-main-section.component';

describe('ProductsMainSectionComponent', () => {
  let component: ProductsMainSectionComponent;
  let fixture: ComponentFixture<ProductsMainSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductsMainSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductsMainSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
