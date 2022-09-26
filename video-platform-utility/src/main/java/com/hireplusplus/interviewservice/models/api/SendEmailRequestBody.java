/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.api;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.enums.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   10-05-2022
 ** @Summary    :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SendEmailRequestBody {

    private Event eventType;
    private String toAddress;
    private String subject;
    private int priority; //1 highest 5 lowest
    private Map<String,String> data;

}
