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

import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   20-07-2022
 ** @ClassName     :    SkillAssessmentInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SkillAssessmentInfo {
    private List<SubSkillAssessmentInfo> subSkillAssessmentInfoList;
    private List<SoftSkillAssessmentInfo> softSkillAssessmentInfoList;
    private String communicationCategory;//assertive//aggressive//passiveAggressive//passive
    private List<SuggestedQuestion> questionsAndAnswersList;

}
