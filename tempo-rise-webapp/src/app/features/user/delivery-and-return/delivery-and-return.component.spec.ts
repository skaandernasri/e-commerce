import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryAndReturnComponent } from './delivery-and-return.component';

describe('DeliveryAndReturnComponent', () => {
  let component: DeliveryAndReturnComponent;
  let fixture: ComponentFixture<DeliveryAndReturnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeliveryAndReturnComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeliveryAndReturnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
