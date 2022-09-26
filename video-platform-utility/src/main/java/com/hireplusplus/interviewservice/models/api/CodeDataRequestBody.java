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
 ** @Created-on    :   12-09-2022
 ** @ClassName     :    CodeDataRequestBody
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class CodeDataRequestBody {

    private String codeString;
    private String compilationString;
}
