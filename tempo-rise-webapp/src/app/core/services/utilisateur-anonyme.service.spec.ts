import { TestBed } from '@angular/core/testing';

import { UtilisateurAnonymeService } from './utilisateur-anonyme.service';

describe('UtilisateurAnonymeService', () => {
  let service: UtilisateurAnonymeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UtilisateurAnonymeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
