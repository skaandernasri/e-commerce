import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PositivitySectionComponent } from './positivity-section.component';

describe('PositivitySectionComponent', () => {
  let component: PositivitySectionComponent;
  let fixture: ComponentFixture<PositivitySectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PositivitySectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PositivitySectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
