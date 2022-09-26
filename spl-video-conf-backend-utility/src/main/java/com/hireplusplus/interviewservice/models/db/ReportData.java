/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.db;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.*;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    FinalReportInfo
 ** @Summary       :
 ****************************************************************************************/
@Data
@ToString
@Slf4j
@Document(collection = "InterviewReport")
public class ReportData {
    @Id
    private String Id;
    @Version
    private int version;
    private String jdTitle;
    private String interviewId;
    private UserInfo candidateInfo;
    private CandidateWorkExperienceInfo candidateExperience;
    private CandidatePreferences candidatePreferences;
    private UserInfo panelistInfo;
    private InterviewInfo interviewInfo;
    private OverallRemarksInfo overallRemarksInfo;
    private SkillAssessmentInfo skillAssessmentInfo;
    private String recordedVideoUrl;
    private String codeString;
    private String compilationString;
    private String jdId;
    private String resumeId;
    private boolean standaloneInterview;
    private String resumeUrl;
    private String jdUrl;



}
