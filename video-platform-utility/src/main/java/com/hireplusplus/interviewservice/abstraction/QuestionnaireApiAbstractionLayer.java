/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.abstraction;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.enums.ResultCode;
import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import com.hireplusplus.interviewservice.models.api.ResultStatusInfo;
import com.hireplusplus.interviewservice.models.db.Question;
import com.hireplusplus.interviewservice.models.db.QuestionnaireSkills;
import com.hireplusplus.interviewservice.repository.QuestionnaireRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   12-08-2022
 ** @ClassName     :    QuestionnaireApiAbstractionLayer
 ** @Summary       :
 ****************************************************************************************/
@Service
@Slf4j
public class QuestionnaireApiAbstractionLayer {

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    public HirePlusPlusResponseBody updateQuestionnaire(MultipartFile questionnaire) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        if (questionnaire == null || questionnaire.isEmpty()) {
            resultStatusInfo.setResultCode(ResultCode.CorruptFile.name());
            resultStatusInfo.setMessage("Questionnaire is NULL/Empty");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(questionnaire.getBytes());
            Workbook questionnaireWorkbook = new XSSFWorkbook(inputStream);
            Iterator<Sheet> sheetIterator = questionnaireWorkbook.sheetIterator();
            Sheet workSheet;
            List<Question> questionsList = new ArrayList<>();
            List<String> skillsList = new ArrayList<>();
            while (sheetIterator.hasNext()) {
                workSheet = sheetIterator.next();
//                log.info(workSheet.getSheetName());
                skillsList.add(workSheet.getSheetName());
                Iterator<Row> rowIterator = workSheet.iterator();
                Row row = rowIterator.next();
                //skipping first row as it contains headers
                while (rowIterator.hasNext()) {
                    row = rowIterator.next();
                    if (row.getCell(0) == null)
                        break;
                    if (row.getCell(0).getCellType().equals(CellType.BLANK))
                        break;
                    if (row.getCell(0).getNumericCellValue() == 0.0)
                        break;

//                    log.info("sl.no " + row.getCell(0).getNumericCellValue());
                    if (row.getCell(1) == null||row.getCell(2)==null)
                        continue;
                    if (row.getCell(1).getCellType().equals(CellType.BLANK)||row.getCell(2).getCellType().equals(CellType.BLANK))
                        continue;

                    Question question = new Question();
                    question.setQuestion(row.getCell(1).getStringCellValue());
                    question.setAnswer(row.getCell(2).getStringCellValue());
                    question.setSkillName(workSheet.getSheetName());


                    question.setTag(workSheet.getSheetName());
                   if(row.getCell(3)!=null){
                       if(!row.getCell(3).getCellType().equals(CellType.BLANK))
                       {
                           question.setTag(row.getCell(3).getStringCellValue());
                       }
                   }

                   //Difficulty level
                    question.setDifficultyLevel("Medium");
                    if(row.getCell(4)!=null){
                        if(!row.getCell(4).getCellType().equals(CellType.BLANK))
                        {
                            question.setTag(row.getCell(4).getStringCellValue());
                        }
                    }

                    questionsList.add(question);

                }
            }
            QuestionnaireSkills questionnaireSkills = new QuestionnaireSkills();
            questionnaireSkills.setSkillsList(skillsList);
            questionnaireRepository.updateQuestionnaireSkills(questionnaireSkills);
            questionnaireRepository.updateQuestionnaire(questionsList);
        } catch (IOException e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode(ResultCode.Failure.name());
            resultStatusInfo.setMessage("Failed to update questionnaire");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Questionnaire successfully updated");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    public HirePlusPlusResponseBody getQuestions(String skillName, int numberOfQuestions) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<Question> data = questionnaireRepository.getQuestionsBySkillName(skillName, numberOfQuestions);
        if(data.size()==0){
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No questions found in DB with skill : "+skillName);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        apiResponse.setData(data);
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Retrieval successful ");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }


    public HirePlusPlusResponseBody getQuestionnaireSkills() {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        QuestionnaireSkills questionnaireSkills = questionnaireRepository.getQuestionnaireSkills();
//        List<String> skillsList = new ArrayList<>();
//        skillsList.add("Java");
//        skillsList.add("Angular");
//        skillsList.add("Spring");
//        questionnaireSkills.setSkillsList(skillsList);
        if(questionnaireSkills.getSkillsList()==null||questionnaireSkills.getSkillsList().size()==0){
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No skills found in the questionnaire DB");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            questionnaireSkills.setSkillsList(new ArrayList<>());
            apiResponse.setData(questionnaireSkills);
            return apiResponse;

        }
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Retrieval successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        apiResponse.setData(questionnaireSkills);
        return apiResponse;
    }

    public HirePlusPlusResponseBody updateQuestionnaireSkills(List<String> questionnaireSkills) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        if(questionnaireSkills==null||questionnaireSkills.size()==0){

            resultStatusInfo.setResultCode("INVALID_PARAMETER");
            resultStatusInfo.setMessage("Skills list is empty");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            apiResponse.setData(questionnaireSkills);
            return apiResponse;
        }

        QuestionnaireSkills dbBody = new QuestionnaireSkills();
        dbBody.setSkillsList(questionnaireSkills);
       dbBody= questionnaireRepository.updateQuestionnaireSkills(dbBody);
       if(dbBody==null||dbBody.getSkillsList().size()!=questionnaireSkills.size()){

           resultStatusInfo.setResultCode(ResultCode.Failure.name());
           resultStatusInfo.setMessage("Insertion Failed partially/fully. inserted skills are sent in data");
           apiResponse.setResultStatusInfo(resultStatusInfo);
           apiResponse.setData(dbBody.getSkillsList());
           return apiResponse;
       }
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Insertion successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        apiResponse.setData(dbBody);
        return apiResponse;
    }
}
