import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationModelManagementComponent } from './recommendation-model-management.component';

describe('RecommendationModelManagementComponent', () => {
  let component: RecommendationModelManagementComponent;
  let fixture: ComponentFixture<RecommendationModelManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommendationModelManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecommendationModelManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
