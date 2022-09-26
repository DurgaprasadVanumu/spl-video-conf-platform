/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.models.db;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;


/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   12-08-2022
 ** @ClassName     :    Questionnaire
 ** @Summary       :
 ****************************************************************************************/
@Data
@Slf4j
@ToString
@Document(collection = "Questionnaire")
public class Question {
    @Id
    private String Id;
    @Version
    private int version;

    private String skillName;
    private String question;
    private String answer;
    private String tag;
    private String difficultyLevel;

}
