import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CodePromoModalComponent } from './code-promo-modal.component';

describe('CodePromoModalComponent', () => {
  let component: CodePromoModalComponent;
  let fixture: ComponentFixture<CodePromoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CodePromoModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CodePromoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
