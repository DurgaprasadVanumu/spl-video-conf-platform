/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.api;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import lombok.Getter;
import lombok.Setter;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    HireplusplusResponseBody
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
public class HirePlusPlusResponseBody<T>{
    private T data;
    private ResultStatusInfo resultStatusInfo;
}
