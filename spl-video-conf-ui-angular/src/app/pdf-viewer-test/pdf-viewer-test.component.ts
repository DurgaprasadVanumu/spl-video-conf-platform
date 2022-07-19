import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-pdf-viewer-test',
  templateUrl: './pdf-viewer-test.component.html',
  styleUrls: ['./pdf-viewer-test.component.scss']
})
export class PdfViewerTestComponent implements OnInit {

  jdSource = "https://images.template.net/wp-content/uploads/2015/11/23162036/Web-Developer-Job-Description-for-Java-Free-PDF-Template.pdf";

  constructor() { }

  ngOnInit(): void {
  }

}
