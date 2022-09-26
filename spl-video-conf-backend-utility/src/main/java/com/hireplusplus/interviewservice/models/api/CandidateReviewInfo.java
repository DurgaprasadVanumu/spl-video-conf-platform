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
 ** @Created-on    :   05-09-2022
 ** @ClassName     :    CandidateReviewInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class CandidateReviewInfo {

    private Object resumeState;

    private String city;
    private String totalExperience;
    private String relevantExperience;
    private List<String> domainExperience;
    private boolean haveOtherOffers;
    private boolean servingNoticePeriod;
    private double recruiterRating;
    private PastEmployers pastEmployers;



}
