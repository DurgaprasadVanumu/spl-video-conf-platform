import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SystemChecksComponent } from './system-checks.component';

describe('SystemChecksComponent', () => {
  let component: SystemChecksComponent;
  let fixture: ComponentFixture<SystemChecksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SystemChecksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SystemChecksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
