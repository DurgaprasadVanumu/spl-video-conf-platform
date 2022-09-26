/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller.client;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import com.hireplusplus.interviewservice.models.api.UpdateResumeStateRequestBody;
import com.hireplusplus.interviewservice.models.api.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   07-09-2022
 ** @ClassName     :    AiServiceClient
 ** @Summary       :
 ****************************************************************************************/
@FeignClient(name = "aiService",url = "${aiService.url}")
public interface AiServiceClient {
    @GetMapping("/jd/getJdDataForInterview")
    public HirePlusPlusResponseBody getJdDataForInterview(@RequestParam String jdId);

    @GetMapping("/resume/review")
    public HirePlusPlusResponseBody getCandidateReviewInfo(@RequestHeader(value = "userId") String userId,
                                                                @RequestParam(name = "jdId") String jdIdentifier,
                                                                @RequestParam(name = "resumeId") String resumeIdentifier);


    @PostMapping("/resume/updateState")
    public HirePlusPlusResponseBody updateResumeState(@RequestBody UpdateResumeStateRequestBody updateResumeStateRequestBody);


}