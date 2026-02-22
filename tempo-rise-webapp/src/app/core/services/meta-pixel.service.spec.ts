import { TestBed } from '@angular/core/testing';

import { MetaPixelService } from './meta-pixel.service';

describe('MetaPixelService', () => {
  let service: MetaPixelService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MetaPixelService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
