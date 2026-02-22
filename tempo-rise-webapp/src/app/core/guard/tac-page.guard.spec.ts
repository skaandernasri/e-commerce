import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { tacPageGuard } from './tac-page.guard';

describe('tacPageGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => tacPageGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
