/****************************************************************************************
 **  						HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller.client;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   27-06-2022
 ** @Summary    :
 ****************************************************************************************/
@FeignClient(name = "schedulerService",url = "${schedulerService.url}")
public interface SchedulerServiceClient {
    @GetMapping("slot/")
    HirePlusPlusResponseBody getSlotData(@RequestParam("slotId") String slotId);
}
