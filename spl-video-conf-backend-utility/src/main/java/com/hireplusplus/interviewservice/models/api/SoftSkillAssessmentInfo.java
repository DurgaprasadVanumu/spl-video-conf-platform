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
 ** @ClassName     :    SoftSkillAssessmentInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SoftSkillAssessmentInfo {

    private String skillName;
    private double skillRating;
}
