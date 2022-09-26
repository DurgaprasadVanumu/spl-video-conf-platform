import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
// import { PdfViewerModule } from 'ng2-pdf-viewer';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { FileuploadComponent } from './fileupload/fileupload.component';
import { MaskingComponent } from './masking/masking.component';

@NgModule({
  declarations: [
    AppComponent,
    FileuploadComponent,
    MaskingComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    // PdfViewerModule,
    NgxExtendedPdfViewerModule,
    HttpClientModule,
    RouterModule.forRoot([
      {  path:'upload',component:FileuploadComponent },
      {  path:'mask',component:MaskingComponent },
      { path: '**', redirectTo: '/upload' },

    ])
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
