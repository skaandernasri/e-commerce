import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BlogsMainSectionComponent } from './blogs-main-section.component';

describe('BlogsMainSectionComponent', () => {
  let component: BlogsMainSectionComponent;
  let fixture: ComponentFixture<BlogsMainSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BlogsMainSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BlogsMainSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
