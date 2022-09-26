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
 ** @Created-on    :   19-08-2022
 ** @ClassName     :    CandidateWorkExperienceInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class CandidateWorkExperienceInfo {

    private List<String> domainExperience;
    private PastEmployers pastEmployers;
    private String relevantExperience;
    private String totalExperience;
}
