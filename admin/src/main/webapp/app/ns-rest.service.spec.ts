import {TestBed} from '@angular/core/testing';

import {NSRESTService} from './ns-rest.service';

describe('NSRESTService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: NSRESTService = TestBed.get(NSRESTService);
    expect(service).toBeTruthy();
  });
});
