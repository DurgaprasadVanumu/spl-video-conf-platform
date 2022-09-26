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
 ** @Created-on    :   20-07-2022
 ** @ClassName     :    SubSkillAssessmentInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SubSkillAssessmentInfo {

    public String skillName;
    private double skillRating;
    private String skillExperience;
    private int skillKnowledge;
    private int skillClarity;
    private String skillRemarks;
    private int skillWeightage;
}
