/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.api;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.enums.ResumeState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   11-08-2022
 ** @ClassName     :    UpdateResumeStateRequestBody
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class UpdateResumeStateRequestBody {
    private String jdIdentifier;
    private String resumeIdentifier;
    private ResumeState resumeState;
}
