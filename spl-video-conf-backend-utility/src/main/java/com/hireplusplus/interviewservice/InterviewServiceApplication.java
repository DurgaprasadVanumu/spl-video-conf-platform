/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :   InterviewServiceApplication
 ** @Summary       :
 ****************************************************************************************/
@SpringBootApplication
@EnableFeignClients
@EnableAsync(proxyTargetClass=true)
public class InterviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewServiceApplication.class, args);
	}

}
