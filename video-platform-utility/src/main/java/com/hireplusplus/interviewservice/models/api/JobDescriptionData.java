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
 ** @ClassName     :    JobDescriptionData
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class JobDescriptionData {
    private String jdIdentifier;
    private String jdTitle;
    private List<SkillData> skillList;

}
