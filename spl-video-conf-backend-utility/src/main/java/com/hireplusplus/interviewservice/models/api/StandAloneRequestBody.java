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
 ** @Created-on    :   19-09-2022
 ** @ClassName     :    StandAloneRequestBody
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class StandAloneRequestBody {

    private List<StandAloneUserInfo> userInfoList;
    private String JdTitle;
    private String clientName;
    private List<SkillData> jdSkillDataList;
    private InterviewInfo interviewInfo;

}
