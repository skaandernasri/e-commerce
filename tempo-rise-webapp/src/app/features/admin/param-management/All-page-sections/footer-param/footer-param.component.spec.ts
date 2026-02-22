import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FooterParamComponent } from './footer-param.component';

describe('FooterParamComponent', () => {
  let component: FooterParamComponent;
  let fixture: ComponentFixture<FooterParamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterParamComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FooterParamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
