import { SocketioService } from './socketio.service';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SampleOneComponent } from './sample-one/sample-one.component';
import { RouterModule, Routes } from '@angular/router';
import { SampleTwoComponent } from './sample-two/sample-two.component';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';
import { PdfViewerTestComponent } from './pdf-viewer-test/pdf-viewer-test.component';
import { HttpClientModule } from '@angular/common/http';
import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { NgxBootstrapSliderModule } from 'ngx-bootstrap-slider';

const routes: Routes = [
  { path: 'sample-one', component: SampleOneComponent },
  { path: 'sample-two/:token', component: SampleTwoComponent },
  { path: 'pdf-viewer', component: PdfViewerTestComponent},
  { path: '', redirectTo: '/sample-one', pathMatch: 'full'}
];

@NgModule({
  declarations: [
    AppComponent,
    SampleOneComponent,
    SampleTwoComponent,
    PdfViewerTestComponent
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    NgbModule,
    RouterModule.forRoot(routes),
    FormsModule,
    NgxExtendedPdfViewerModule,
    HttpClientModule,
    NgxSliderModule,
    NgxBootstrapSliderModule
  ],
  providers: [SocketioService],
  bootstrap: [AppComponent]
})
export class AppModule { }
