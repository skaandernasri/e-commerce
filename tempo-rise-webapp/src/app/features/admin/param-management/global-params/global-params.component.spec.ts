import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalParamsComponent } from './global-params.component';

describe('GlobalParamsComponent', () => {
  let component: GlobalParamsComponent;
  let fixture: ComponentFixture<GlobalParamsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GlobalParamsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GlobalParamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
