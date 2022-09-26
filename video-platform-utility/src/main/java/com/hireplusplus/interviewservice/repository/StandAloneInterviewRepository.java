/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.repository;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.db.ReportData;
import com.hireplusplus.interviewservice.models.db.StandAloneInterviewData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   20-09-2022
 ** @ClassName     :    StandAloneInterviewRepository
 ** @Summary       :
 ****************************************************************************************/
@Repository
public class StandAloneInterviewRepository {
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String INTERVIEW_ID = "interviewId";


    public StandAloneInterviewData saveStandAloneInterviewData(StandAloneInterviewData standAloneInterviewData) {
        return mongoTemplate.save(standAloneInterviewData);
    }

    public StandAloneInterviewData updateStandAloneInterviewData(StandAloneInterviewData standAloneInterviewData) {
        Query query = new Query(Criteria.where(INTERVIEW_ID).is(standAloneInterviewData.getInterviewId()));
        return mongoTemplate.findAndReplace(query, standAloneInterviewData);
    }

    public StandAloneInterviewData getStandAloneInterviewDataByJdId(String jdId){
        Query query = new Query(Criteria.where("jdId").is(jdId));
        return mongoTemplate.findOne(query, StandAloneInterviewData.class);

    }

    public StandAloneInterviewData getStandAloneInterviewData(String interviewId){
        Query query = new Query(Criteria.where(INTERVIEW_ID).is(interviewId));
        return mongoTemplate.findOne(query, StandAloneInterviewData.class);

    }
}
