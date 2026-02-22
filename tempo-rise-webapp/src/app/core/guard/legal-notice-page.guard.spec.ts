import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { legalNoticePageGuard } from './legal-notice-page.guard';

describe('legalNoticePageGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => legalNoticePageGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
