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
 ** @Created-on    :   19-08-2022
 ** @ClassName     :    CandidatePreferences
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class CandidatePreferences {

   private boolean onSiteExperience;
    private boolean openessForOnsite;
    private int workingModel; //1=HYBRID,2=WFH,3=WFO
}
