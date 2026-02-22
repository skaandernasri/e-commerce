import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { productsPageGuardGuard } from './products-page-guard.guard';

describe('productsPageGuardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => productsPageGuardGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
