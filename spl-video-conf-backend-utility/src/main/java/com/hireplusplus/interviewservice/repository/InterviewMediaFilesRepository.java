/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.repository;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.db.InterviewMediaFilesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   10-08-2022
 ** @ClassName     :    interviewRepository
 ** @Summary       :
 ****************************************************************************************/
@Repository
public class InterviewMediaFilesRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public InterviewMediaFilesData getInterviewMediaFilesInfoById(String interviewId){
        Query query = new Query(Criteria.where("interviewId").is(interviewId));
        return mongoTemplate.findOne(query, InterviewMediaFilesData.class);
    }

    public void saveInterviewCompleteInfo(InterviewMediaFilesData interviewCompleteInfo) {
        mongoTemplate.save(interviewCompleteInfo);
    }

    public InterviewMediaFilesData updateInterviewMediaFilesInfo(String interviewId, InterviewMediaFilesData interviewCompleteInfo) {
        Query query = new Query(Criteria.where("interviewId").is(interviewId));
        mongoTemplate.findAndReplace(query,interviewCompleteInfo);
        return mongoTemplate.findOne(query, InterviewMediaFilesData.class);
    }
}
