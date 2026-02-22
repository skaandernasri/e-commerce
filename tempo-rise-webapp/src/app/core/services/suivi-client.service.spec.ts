import { TestBed } from '@angular/core/testing';

import { SuiviClientServiceService } from './suivi-client.service';

describe('SuiviClientServiceService', () => {
  let service: SuiviClientServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SuiviClientServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
