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
 ** @Created-on    :   08-08-2022
 ** @ClassName     :    SuggestedQuestion
 ** @Summary       :
 ****************************************************************************************/
@Getter
@Setter
@ToString
public class SuggestedQuestion {

    private String question;
    private String modelAnswer;
    private String difficultyLevel;
    private int answerRating;
}
