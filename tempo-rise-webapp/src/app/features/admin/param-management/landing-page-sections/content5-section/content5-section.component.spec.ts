import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Content5SectionComponent } from './content5-section.component';

describe('Content5SectionComponent', () => {
  let component: Content5SectionComponent;
  let fixture: ComponentFixture<Content5SectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Content5SectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Content5SectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
