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
 ** @Created-on    :   19-08-2022
 ** @ClassName     :    PastEmployers
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class PastEmployers {
    private boolean mnc;
    private boolean largeFirms;
    private boolean sme;
    private boolean startUps;

}
