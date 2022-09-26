/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireplusplus.interviewservice.abstraction.ReportApiAbstractionLayer;
import com.hireplusplus.interviewservice.enums.ResultCode;
import com.hireplusplus.interviewservice.models.api.*;
import com.hireplusplus.interviewservice.models.db.ReportData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import java.nio.charset.StandardCharsets;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    ReportController
 ** @Summary       :
 ****************************************************************************************/
@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/report")
@Slf4j
public class ReportController {

    @Autowired
    private ReportApiAbstractionLayer reportApiAbstractionLayer;

    @GetMapping("/")
    public HirePlusPlusResponseBody getReportData(@RequestParam String interviewId) {
        return reportApiAbstractionLayer.getReportData(interviewId);
    }

    @PostMapping("/")
    public HirePlusPlusResponseBody updateReportData(@RequestBody ReportData reportData) {
        return reportApiAbstractionLayer.updateReportData(reportData);
    }

//    @GetMapping("/getSample")
//    public String sampleReport(){
//        return reportApiAbstractionLayer.getSampleReportHtml();
//    }

    @GetMapping("/test")
    public String test() {
        return "report controller invoked";
    }

    @GetMapping("/getCompleteInterviewData")
    public HirePlusPlusResponseBody getCompleteInterviewData(@RequestParam("slotId") String slotId) {
        return reportApiAbstractionLayer.getCompleteInterviewData(slotId);
    }

    @GetMapping("/getInterviewJdData")
    public JobDescriptionData getJdData(@RequestParam("jdId") String jdId) {
        return reportApiAbstractionLayer.getJdData(jdId);

    }

    @GetMapping("/getCandidateReviewInfo")
    public CandidateReviewInfo getCandidateReviewInfo(@RequestParam String jdId, @RequestParam String candidateId, @RequestParam String recruiterId) {
        return reportApiAbstractionLayer.getCandidateReviewInfo(jdId, candidateId, recruiterId);

    }

    @PostMapping("/saveCode")
    public HirePlusPlusResponseBody saveCode(@RequestParam String interviewId, @RequestBody CodeDataRequestBody codeDataRequestBody) {
        return reportApiAbstractionLayer.saveCode(interviewId, codeDataRequestBody);

    }

    @GetMapping("/streamCode")
    public HirePlusPlusResponseBody streamCode(@RequestParam String interviewId, HttpServletResponse servletResponse) {
        return reportApiAbstractionLayer.streamCode(interviewId, servletResponse);

    }

    @PostMapping("/standAlone/createRoom")
    public HirePlusPlusResponseBody createStandAloneInterviewRoom(@FormParam("resumeFile") MultipartFile resumeFile,
                                                                  @FormParam("jdFile") MultipartFile jdFile,
                                                                  @FormParam("standAloneRequestBody") String standAloneRequestBody) {

        StandAloneRequestBody standAloneRequestBodyObj =null;
        try {
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.findAndRegisterModules();
            standAloneRequestBodyObj = objMapper.readValue(standAloneRequestBody, StandAloneRequestBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
            ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
            resultStatusInfo.setResultCode("BAD_REQUEST");
            resultStatusInfo.setMessage("Failed to parse the requestBody");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        log.info(standAloneRequestBodyObj.toString());


        return reportApiAbstractionLayer.createStandAloneInterviewRoom(resumeFile, jdFile, standAloneRequestBodyObj);

    }

    @GetMapping("/standAlone/resume")
    public HirePlusPlusResponseBody downloadStandAloneResume(@RequestParam String jdId,@RequestParam String resumeId, HttpServletResponse servletResponse){
        return  reportApiAbstractionLayer.downloadStandAloneResume(jdId,resumeId,servletResponse);

    }
    @GetMapping("/standAlone/jd")
    public HirePlusPlusResponseBody downloadStandAloneJd(@RequestParam String jdId,HttpServletResponse servletResponse){

        return  reportApiAbstractionLayer.downloadStandAloneJd(jdId,servletResponse);
    }


    @GetMapping("/text")
    public void streamTextFile(HttpServletResponse servletResponse) {
        try {
            String abc = "code info\ncode info line 2";
            String def = "\ncompilation info";
            ServletOutputStream stream = servletResponse.getOutputStream();
            stream.write(abc.getBytes(StandardCharsets.UTF_8));
            stream.write(def.getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @GetMapping("/test2")
//    public JobDescriptionData test2(){
//        return reportApiAbstractionLayer.test2();
//    }
}
