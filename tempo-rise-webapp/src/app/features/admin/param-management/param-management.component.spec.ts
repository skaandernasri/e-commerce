import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParamManagementComponent } from './param-management.component';

describe('ParamManagementComponent', () => {
  let component: ParamManagementComponent;
  let fixture: ComponentFixture<ParamManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParamManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParamManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
