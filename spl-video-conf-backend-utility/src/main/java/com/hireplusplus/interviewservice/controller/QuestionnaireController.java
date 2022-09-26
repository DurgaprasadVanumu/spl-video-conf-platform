/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.controller;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.abstraction.QuestionnaireApiAbstractionLayer;
import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.FormParam;
import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   12-08-2022
 ** @ClassName     :    QuestionnaireController
 ** @Summary       :
 ****************************************************************************************/
@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/questionnaire")
@Slf4j
public class QuestionnaireController {

    @Autowired
    private QuestionnaireApiAbstractionLayer questionnaireApiAbstractionLayer;

    @PostMapping("/update")
    public HirePlusPlusResponseBody updateQuestionnaire(@FormParam("questionnaire") MultipartFile questionnaire) {

        return questionnaireApiAbstractionLayer.updateQuestionnaire(questionnaire);
    }

    @GetMapping("/get")
    public HirePlusPlusResponseBody getQuestions(@RequestParam String skillName,@RequestParam int numberOfQuestions) {

        return questionnaireApiAbstractionLayer.getQuestions(skillName,numberOfQuestions);
    }

    @PostMapping("/skills")
    public  HirePlusPlusResponseBody updateQuestionnaireSkills(@RequestBody List<String> questionnaireSkills){
        return  questionnaireApiAbstractionLayer.updateQuestionnaireSkills(questionnaireSkills);
    }

    @GetMapping("/skills")
    public HirePlusPlusResponseBody getQuestionnaireSkills(){
        return questionnaireApiAbstractionLayer.getQuestionnaireSkills();
    }
}
