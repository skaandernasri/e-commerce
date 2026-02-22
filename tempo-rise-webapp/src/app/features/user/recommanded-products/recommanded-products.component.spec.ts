import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommandedProductsComponent } from './recommanded-products.component';

describe('RecommandedProductsComponent', () => {
  let component: RecommandedProductsComponent;
  let fixture: ComponentFixture<RecommandedProductsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommandedProductsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecommandedProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
