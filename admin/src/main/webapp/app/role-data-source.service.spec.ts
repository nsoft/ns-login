import {TestBed} from '@angular/core/testing';

import {RoleDataSourceService} from './role-data-source.service';

describe('RoleDataSourceService', () => {
  let service: RoleDataSourceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RoleDataSourceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
