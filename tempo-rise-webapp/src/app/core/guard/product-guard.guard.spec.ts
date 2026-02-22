import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { productGuardGuard } from './product-guard.guard';

describe('productGuardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => productGuardGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
