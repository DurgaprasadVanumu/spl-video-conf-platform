/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.service;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.*;
import com.hireplusplus.interviewservice.models.db.ReportData;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    ReportApiHandler
 ** @Summary       :
 ****************************************************************************************/
public interface ReportApiHandler {
    String generateReportHtml(GenerateReportRequestBody generateReportRequestBody);

    String getSampleReportHtml();

    HirePlusPlusResponseBody getCompleteInterviewData(String slotId);

    JobDescriptionData populateJdData(String abcd);

    HirePlusPlusResponseBody getReportData(String interviewId);

    HirePlusPlusResponseBody updateReportData(ReportData reportData);

    CandidateReviewInfo populateCandidateReviewInfo(String jdId, String candidateId, String recruiterId);

    HirePlusPlusResponseBody streamCode(String interviewId, HttpServletResponse servletResponse);

    HirePlusPlusResponseBody saveCode(String interviewId, CodeDataRequestBody codeDataRequestBody);

    HirePlusPlusResponseBody createStandAloneInterviewRoom(MultipartFile resumeFile, MultipartFile jdFile, StandAloneRequestBody standAloneRequestBody);

    HirePlusPlusResponseBody downloadStandAloneResume(String jdId, String resumeId, HttpServletResponse servletResponse);

    HirePlusPlusResponseBody downloadStandAloneJd(String jdId, HttpServletResponse servletResponse);
}
