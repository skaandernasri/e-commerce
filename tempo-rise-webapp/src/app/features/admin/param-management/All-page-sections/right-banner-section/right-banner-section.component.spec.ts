import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RightBannerSectionComponent } from './right-banner-section.component';

describe('RightBannerSectionComponent', () => {
  let component: RightBannerSectionComponent;
  let fixture: ComponentFixture<RightBannerSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RightBannerSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RightBannerSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
