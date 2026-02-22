import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewProductsComponent } from './products-management.component';

describe('ViewProductsComponent', () => {
  let component: ViewProductsComponent;
  let fixture: ComponentFixture<ViewProductsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewProductsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
