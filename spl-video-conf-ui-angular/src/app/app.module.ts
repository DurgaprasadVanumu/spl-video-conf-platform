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
import { InterviewUtilityApiService } from './interview-utility-api.service';
import { SafePipe } from './safe.pipe';
import { ReportComponent } from './report/report.component';
import { NgxPrintModule } from 'ngx-print';
import { WindowCloseGuard } from './sample-two/window-close-guard';
import { SystemChecksComponent } from './system-checks/system-checks.component';
import { CandidateComponent } from './candidate/candidate.component';
import { InterviewerComponent } from './interviewer/interviewer.component';
import { ScreenRecordingService } from './screen-recording.service';

const routes: Routes = [
  { path: 'candidate', component: CandidateComponent},
  { path: 'interviewer', component: InterviewerComponent},
  { path: 'system-checks', component: SystemChecksComponent},
  { path: 'join-interview', component: SampleOneComponent },
  { path: 'sample-two/:token', component: SampleTwoComponent , canDeactivate: [WindowCloseGuard]},
  { path: 'pdf-viewer', component: PdfViewerTestComponent},
  { path: '', redirectTo: '/join-interview', pathMatch: 'full'},
  { path:'report',component:ReportComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    SampleOneComponent,
    SampleTwoComponent,
    PdfViewerTestComponent,
    SafePipe,
    ReportComponent,
    SystemChecksComponent,
    CandidateComponent,
    InterviewerComponent
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
    NgxBootstrapSliderModule,
    NgxPrintModule
  ],
  providers: [SocketioService, InterviewUtilityApiService, WindowCloseGuard,ScreenRecordingService],
  bootstrap: [AppComponent]
})
export class AppModule { }
