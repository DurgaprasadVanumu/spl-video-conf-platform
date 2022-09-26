import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdfViewerTestComponent } from './pdf-viewer-test.component';

describe('PdfViewerTestComponent', () => {
  let component: PdfViewerTestComponent;
  let fixture: ComponentFixture<PdfViewerTestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PdfViewerTestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PdfViewerTestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
