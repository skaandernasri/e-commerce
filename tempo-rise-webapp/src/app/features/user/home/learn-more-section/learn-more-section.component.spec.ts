import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LearnMoreSectionComponent } from './learn-more-section.component';

describe('LearnMoreSectionComponent', () => {
  let component: LearnMoreSectionComponent;
  let fixture: ComponentFixture<LearnMoreSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LearnMoreSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LearnMoreSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
