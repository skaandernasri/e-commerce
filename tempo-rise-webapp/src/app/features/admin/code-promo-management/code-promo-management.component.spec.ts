import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CodePromoManagementComponent } from './code-promo-management.component';

describe('CodePromoManagementComponent', () => {
  let component: CodePromoManagementComponent;
  let fixture: ComponentFixture<CodePromoManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CodePromoManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CodePromoManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
