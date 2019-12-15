import {TestBed} from '@angular/core/testing';

import {UserDataSourceService} from './user-data-source.service';

describe('UserDataSourceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UserDataSourceService = TestBed.get(UserDataSourceService);
    expect(service).toBeTruthy();
  });
});
