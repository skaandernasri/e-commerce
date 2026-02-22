import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { deliveryAndReturnPageGuard } from './delivery-and-return-page.guard';

describe('deliveryAndReturnPageGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => deliveryAndReturnPageGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
