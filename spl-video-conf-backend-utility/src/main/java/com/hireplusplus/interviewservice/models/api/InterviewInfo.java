/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.api;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   21-07-2022
 ** @ClassName     :    InterviewInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class InterviewInfo {

//    private String interviewId;
//    private String panelistName;
//    private String panelistId;
//    private String panelistLocation;
//    private double panelistExperience;
    private String timeZone;
    private String interviewDate;//format dd-mm-yyyy
    private String interviewStartTime;//HH:MM
    private String interviewEndTime;//HH:MM
}
