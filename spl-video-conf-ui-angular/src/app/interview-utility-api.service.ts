import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CompileCode } from './models/compile-code';
import { ReportGenerator } from './models/generate-report';
import { HireplusplusResponse } from './models/hireplusplus-response';

@Injectable({
  providedIn: 'root'
})
export class InterviewUtilityApiService {
  INTERVIEW_UTILITY_SERVICE_HOST = environment.INTERVIEW_UTILITY_SERVICE_API;
  AI_SERVICE_HOST = environment.AI_SERVICE_API;

  GET_ALL_INTERVIEW_DETAILS_API = "/report/getCompleteInterviewData";

  constructor(private httpClient: HttpClient) { }

  compileCode(body: CompileCode){
    let url = "http://20.232.38.111:8082/api/v1/java/compile"
    return this.httpClient.post(url, body);
  }

  generateReportInHtml(reportGenerator: ReportGenerator):Observable<string>{
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+"/report/";
    return this.httpClient.post<string>(url, reportGenerator);
  }

  getInterviewDetails(interviewID: string): Observable<HireplusplusResponse>{
    // let url = this.INTERVIEW_UTILITY_SERVICE_HOST+this.GET_ALL_INTERVIEW_DETAILS_API+'?slotId='+interviewID;
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+'/report/getCompleteInterviewData?slotId='+interviewID;
    return this.httpClient.get<HireplusplusResponse>(url);
  }

  downloadJd(standAloneInterview:boolean,jdId: string): Observable<ArrayBuffer>{
    let url="";
    if(standAloneInterview){
      url = this.INTERVIEW_UTILITY_SERVICE_HOST+'/report/standAlone/jd?jdId='+jdId;

    }else{
      url = this.AI_SERVICE_HOST+'/jd/download?jdId='+jdId;

    }
    return this.httpClient.get(url, {responseType: 'arraybuffer'});
  }

  downloadCandidateResume(standAloneInterview:boolean, jdId: string, resumeId: string): Observable<ArrayBuffer>{
    let url="";
    if(standAloneInterview){
      url = this.INTERVIEW_UTILITY_SERVICE_HOST+'/report/standAlone/resume?jdId='+jdId+'&resumeId='+resumeId;

    }else{
      url = this.AI_SERVICE_HOST+'/resume/download?jdId='+jdId+'&resumeId='+resumeId;

    }
    return this.httpClient.get(url, {responseType: 'arraybuffer'});
  }

  getBase64Snapshots(interviewId:string){
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+"/interview/snapshots";
    let parameters:any={'interviewId':interviewId};
    return this.httpClient.get(url,{params:parameters});
  }


  uploadSnapshots(interviewId:string,formData:FormData,options){
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+"/interview/upload/snapshot?interviewId="+interviewId;
    return this.httpClient.post(url,formData,options)
  }

  getReportData(interviewId:string){
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+"/report/?interviewId="+interviewId;
    let parameters:any={'interviewId':interviewId};
    return this.httpClient.get(url);
  }

  updateReportData(reportData:ReportGenerator){
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+"/report/";
    return this.httpClient.post(url,reportData);
  }

  endInterview(interviewId: string){
    let url = this.INTERVIEW_UTILITY_SERVICE_HOST+"/interview/end?interviewId="+interviewId;
    return this.httpClient.post(url, {});
  }

}
