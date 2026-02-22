import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content7SectionComponent } from './content7-section.component';

describe('Content7SectionComponent', () => {
  let component: Content7SectionComponent;
  let fixture: ComponentFixture<Content7SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content7SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content7SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
