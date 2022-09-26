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
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    GenerateReportRequestBody
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class GenerateReportRequestBody {
    private String interviewId;
//    private CandidateInfo candidateInfo;
//    private panelistinfo panelistInfo;
    private InterviewInfo interviewInfo;
    private SkillAssessmentInfo skillAssessmentInfo;
    private OverallRemarksInfo overallRemarksInfo;

}
