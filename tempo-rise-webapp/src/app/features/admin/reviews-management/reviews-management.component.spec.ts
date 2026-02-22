import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewsManagementComponent } from './reviews-management.component';

describe('ReviewsManagementComponent', () => {
  let component: ReviewsManagementComponent;
  let fixture: ComponentFixture<ReviewsManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewsManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
