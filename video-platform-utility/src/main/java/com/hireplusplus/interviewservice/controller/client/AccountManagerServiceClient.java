/****************************************************************************************
 **  						HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller.client;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import com.hireplusplus.interviewservice.models.api.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   27-06-2022
 ** @Summary    :
 ****************************************************************************************/
@FeignClient(name = "accountManagerService",url = "${accountManagerService.url}")
public interface AccountManagerServiceClient {
    @GetMapping("/account/getUserDetails/{id}")
    public HirePlusPlusResponseBody getUserDetails(@PathVariable("id") String id);
    @GetMapping("/candidate/getDetails")
    public HirePlusPlusResponseBody getCandidateDetails(@RequestParam(name="email") String email,@RequestParam(name="mobile") String mobile);
    @PostMapping("/account/getUsersDetails")
    public HirePlusPlusResponseBody<List<UserInfo>> getUsersDetails(@RequestBody List<String> userIdList);
}
