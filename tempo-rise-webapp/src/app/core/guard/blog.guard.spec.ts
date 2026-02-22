import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { blogGuard } from './blog.guard';

describe('blogGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => blogGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
