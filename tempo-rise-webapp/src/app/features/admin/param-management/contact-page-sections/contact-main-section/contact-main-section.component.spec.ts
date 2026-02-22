import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactMainSectionComponent } from './contact-main-section.component';

describe('ContactMainSectionComponent', () => {
  let component: ContactMainSectionComponent;
  let fixture: ComponentFixture<ContactMainSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactMainSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContactMainSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
