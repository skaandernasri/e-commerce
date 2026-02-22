import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { aboutPageGuardGuard } from './about-page-guard.guard';

describe('aboutPageGuardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => aboutPageGuardGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
