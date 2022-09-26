/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.repository;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.db.Question;
import com.hireplusplus.interviewservice.models.db.QuestionnaireSkills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   08-08-2022
 ** @ClassName     :    QuestionaireRepository
 ** @Summary       :
 ****************************************************************************************/
@Repository
public class QuestionnaireRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean updateQuestionnaire(List<Question> questionnaire) {
        mongoTemplate.dropCollection(Question.class);
        int insertCount = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,Question.class).insert(questionnaire).execute().getInsertedCount();

        if(insertCount==questionnaire.size()){
            return true;
        }else
            return false;
    }

    public List<Question> getQuestionsBySkillName(String skillName,int numberOfQuestions){

        MatchOperation matchStage = Aggregation.match(Criteria.where("skillName").is(skillName));
        SampleOperation sampleStage = Aggregation.sample(numberOfQuestions);
        Aggregation aggregation = Aggregation.newAggregation(matchStage,sampleStage);
        AggregationResults<Question> output = mongoTemplate.aggregate(aggregation, "Questionnaire", Question.class);

        return output.getMappedResults();
//        Query query = new Query(Criteria.where("skillName").is(skillName));
//        query.limit(numberOfQuestions);
//        return mongoTemplate.find(query, Question.class);
    }

    public QuestionnaireSkills updateQuestionnaireSkills(QuestionnaireSkills questionnaireSkills){
      mongoTemplate.dropCollection(QuestionnaireSkills.class);
      return mongoTemplate.save(questionnaireSkills);

    }

    public QuestionnaireSkills getQuestionnaireSkills(){
        List<QuestionnaireSkills> skills = mongoTemplate.findAll(QuestionnaireSkills.class);
        System.out.println(skills);
        if(skills==null||skills.size()==0){
            return new QuestionnaireSkills();
        }else{
            return skills.get(0);
        }

    }


}
