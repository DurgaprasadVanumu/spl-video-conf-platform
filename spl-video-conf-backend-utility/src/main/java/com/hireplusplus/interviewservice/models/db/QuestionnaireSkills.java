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
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   21-09-2022
 ** @ClassName     :    SkillList
 ** @Summary       :
 ****************************************************************************************/
@Data
@ToString
@Slf4j
@Document(collection = "QuestionnaireSkill")
public class QuestionnaireSkills {
    @Id
    private String Id;
    @Version
    private int version;
    private List<String> skillsList;
}
