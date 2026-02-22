import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewLetterSectionComponent } from './new-letter-section.component';

describe('NewLetterSectionComponent', () => {
  let component: NewLetterSectionComponent;
  let fixture: ComponentFixture<NewLetterSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewLetterSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewLetterSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
