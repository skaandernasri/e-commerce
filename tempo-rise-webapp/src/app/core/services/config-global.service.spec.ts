import { TestBed } from '@angular/core/testing';

import { ConfigGlobalService } from './config-global.service';

describe('ConfigGlobalService', () => {
  let service: ConfigGlobalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigGlobalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
