/****************************************************************************************
 **  						HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller.client;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.SendEmailRequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   27-06-2022
 ** @Summary    :
 ****************************************************************************************/
@FeignClient(name = "communicationService",url = "${communicationService.url}")
public interface CommunicationServiceClient {
    @PostMapping("/email/")
    public void sendEmail(@RequestBody SendEmailRequestBody emailRequestBody);
}
