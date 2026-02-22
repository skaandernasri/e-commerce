import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvoicesManagementComponent } from './invoices-management.component';

describe('InvoicesManagementComponent', () => {
  let component: InvoicesManagementComponent;
  let fixture: ComponentFixture<InvoicesManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvoicesManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvoicesManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
