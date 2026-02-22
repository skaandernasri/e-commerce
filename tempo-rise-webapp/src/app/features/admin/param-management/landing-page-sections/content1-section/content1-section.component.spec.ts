import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content1SectionComponent } from './content1-section.component';

describe('Content1SectionComponent', () => {
  let component: Content1SectionComponent;
  let fixture: ComponentFixture<Content1SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content1SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content1SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
