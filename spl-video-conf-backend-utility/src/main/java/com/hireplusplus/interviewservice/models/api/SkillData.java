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
 ** @Created-on    :   08-08-2022
 ** @ClassName     :    SkillData
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SkillData {

    private String skillName;
    private int skillWeightage;
    private List<SuggestedQuestion> suggestedQuestionList;
}
