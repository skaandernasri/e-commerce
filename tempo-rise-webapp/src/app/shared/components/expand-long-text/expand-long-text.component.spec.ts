import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpandLongTextComponent } from './expand-long-text.component';

describe('ExpandLongTextComponent', () => {
  let component: ExpandLongTextComponent;
  let fixture: ComponentFixture<ExpandLongTextComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExpandLongTextComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExpandLongTextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
