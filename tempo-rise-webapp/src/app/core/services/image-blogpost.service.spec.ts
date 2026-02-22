import { TestBed } from '@angular/core/testing';

import { ImageBlogpostService } from './image-blogpost.service';

describe('ImageBlogpostService', () => {
  let service: ImageBlogpostService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ImageBlogpostService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
