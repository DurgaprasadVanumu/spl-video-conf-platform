import { TestBed } from '@angular/core/testing';

import { InterviewUtilityApiService } from './interview-utility-api.service';

describe('InterviewUtilityApiService', () => {
  let service: InterviewUtilityApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InterviewUtilityApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
