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
 ** @Created-on    :   07-09-2022
 ** @ClassName     :    InterviewSkillData
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class InterviewSkillItem {

    private String skillName;
    private int skillWeightage;

}
