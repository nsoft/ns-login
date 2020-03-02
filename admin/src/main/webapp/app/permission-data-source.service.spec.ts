import {TestBed} from '@angular/core/testing';

import {PermissionDataSourceService} from './permission-data-source.service';

describe('PermissionDataSourceService', () => {
  let service: PermissionDataSourceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PermissionDataSourceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
