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
 ** @ClassName     :    CompleteInterviewData
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class InterviewInitData {

    private JobDescriptionData jdData;
    private InterviewInfo interviewInfo;
    private List<UserInfo> userInfoList;
    private String recordedVideoUrl;
    private CandidateReviewInfo candidateReviewInfo;
    private String jdUrl;
    private String resumeUrl;
    private boolean standaloneInterview;
}
