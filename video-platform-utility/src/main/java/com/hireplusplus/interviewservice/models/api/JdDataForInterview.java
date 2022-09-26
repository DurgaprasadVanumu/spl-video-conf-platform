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
 ** @Created-on    :   08-09-2022
 ** @ClassName     :    JdDataForInterview
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class JdDataForInterview {
    private String jdId;
    private String jdTitle;
    private String clientName;
    private List<InterviewSkillItem> jdSkillDataList;
}
