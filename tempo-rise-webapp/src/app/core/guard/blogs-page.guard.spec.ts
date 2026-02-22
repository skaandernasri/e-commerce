import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { blogsPageGuard } from './blogs-page.guard';

describe('blogsPageGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => blogsPageGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
