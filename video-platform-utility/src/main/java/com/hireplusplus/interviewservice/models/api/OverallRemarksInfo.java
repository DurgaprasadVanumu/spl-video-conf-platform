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
 ** @ClassName     :    OverallRemarksInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class OverallRemarksInfo {
    private String overallRemarks;
    private double communication;
    private double technicalSkills;
    private double enthusiasm;
    private double softSkills;
    private double attitude;
}
