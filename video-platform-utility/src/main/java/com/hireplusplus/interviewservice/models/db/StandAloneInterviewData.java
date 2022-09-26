/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.db;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.api.InterviewInfo;
import com.hireplusplus.interviewservice.models.api.SkillData;
import com.hireplusplus.interviewservice.models.api.UserInfo;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   20-09-2022
 ** @ClassName     :    StandAloneInterviewData
 ** @Summary       :
 ****************************************************************************************/
@Data
@ToString
@Slf4j
@Document(collection = "StandAloneInterviewData")
public class StandAloneInterviewData {

    @Id
    private String Id;
    @Version
    private int version;

    private String interviewId;
    private String jdId;
    private String jdTitle;
    private String resumeId;
    private String resumeUrl;
    private String resumeExtension;
    private String jdExtension;
    private String jdUrl;
    private List<UserInfo> userInfoList;//standalone
    private List<SkillData> skillDataList;//standalone
    private InterviewInfo interviewInfo;

}
