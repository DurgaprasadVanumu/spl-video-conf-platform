/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.abstraction;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.*;
import com.hireplusplus.interviewservice.models.db.ReportData;
import com.hireplusplus.interviewservice.repository.ReportRepository;
import com.hireplusplus.interviewservice.service.ReportApiHandler;
import com.hireplusplus.interviewservice.service.impl.ReportApiHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    ReportApiAbstractionLayer
 ** @Summary       :
 ****************************************************************************************/
@Service
@Slf4j
public class ReportApiAbstractionLayer {

    private ReportApiHandler reportApiHandler;
    @Autowired
    private ReportApiHandlerImpl reportApiHandlerImpl;
    @Autowired
    private ReportRepository reportRepo;

    private ReportApiHandler getReportApiHandler(){
        return reportApiHandlerImpl;
    }

    public HirePlusPlusResponseBody getCompleteInterviewData(String slotId) {
        reportApiHandler = getReportApiHandler();
        return reportApiHandler.getCompleteInterviewData(slotId);
    }

    public HirePlusPlusResponseBody getReportData(String interviewId) {
        reportApiHandler = getReportApiHandler();
        return reportApiHandler.getReportData(interviewId);
    }

    public HirePlusPlusResponseBody updateReportData(ReportData reportData) {
        reportApiHandler = getReportApiHandler();
        return reportApiHandler.updateReportData(reportData);
    }

    public JobDescriptionData getJdData(String jdId){
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.populateJdData(jdId);

    }

    public CandidateReviewInfo getCandidateReviewInfo(String jdId, String candidateId, String recruiterId){
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.populateCandidateReviewInfo(jdId, candidateId, recruiterId);

    }

    public HirePlusPlusResponseBody streamCode(String interviewId, HttpServletResponse servletResponse) {
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.streamCode(interviewId,servletResponse);
    }

    public HirePlusPlusResponseBody saveCode(String interviewId, CodeDataRequestBody codeDataRequestBody) {
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.saveCode(interviewId,codeDataRequestBody);
    }

    public HirePlusPlusResponseBody createStandAloneInterviewRoom(MultipartFile resumeFile, MultipartFile jdFile, StandAloneRequestBody standAloneRequestBody) {
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.createStandAloneInterviewRoom(resumeFile,jdFile,standAloneRequestBody);
    }

    public HirePlusPlusResponseBody downloadStandAloneResume(String jdId, String resumeId, HttpServletResponse servletResponse) {
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.downloadStandAloneResume(jdId,resumeId,servletResponse);
    }

    public HirePlusPlusResponseBody downloadStandAloneJd(String jdId, HttpServletResponse servletResponse) {
        reportApiHandler=getReportApiHandler();
        return reportApiHandler.downloadStandAloneJd(jdId,servletResponse);
    }


//    public HirePlusPlusResponseBody getReportHtml(String interviewId){
//        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
//        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
//        ReportData reportRepoData = reportRepo.getReportInfo(interviewId);
//        if(reportRepoData==null){
//            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
//            resultStatusInfo.setMessage("No Report found with ID : "+interviewId);
//            apiResponse.setResultStatusInfo(resultStatusInfo);
//            return apiResponse;
//        }
//
//        resultStatusInfo.setResultCode(ResultCode.Success.name());
//        resultStatusInfo.setMessage("Retrieval Successful");
////        apiResponse.setData(reportRepoData.getReportHtml());
//        apiResponse.setResultStatusInfo(resultStatusInfo);
//        return apiResponse;
//    }

//    public String generateReportHtml(GenerateReportRequestBody generateReportRequestBody) {
//        reportApiHandler = getReportApiHandler();
//        return reportApiHandler.generateReportHtml(generateReportRequestBody);
//    }

//    public String getSampleReportHtml() {
//        reportApiHandler = getReportApiHandler();
//        return reportApiHandler.getSampleReportHtml();
//    }


}
