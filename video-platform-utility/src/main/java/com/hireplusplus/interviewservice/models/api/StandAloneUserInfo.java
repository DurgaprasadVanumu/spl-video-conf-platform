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
 ** @Created-on    :   16-09-2022
 ** @ClassName     :    StandAloneUserInfo
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class StandAloneUserInfo {
    private String email;
    private String mobile;
    private String userRole;
    private String firstName;
    private String lastName;

}
