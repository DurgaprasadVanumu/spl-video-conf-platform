/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.repository;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.models.db.ReportData;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Repository;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    ReportRepository
 ** @Summary       :
 ****************************************************************************************/
@Repository
public class ReportRepository {
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String INTERVIEW_ID = "interviewId";

    public ReportData saveReport(ReportData reportInfo) {
        return mongoTemplate.save(reportInfo);
    }

    public ReportData updateReport(ReportData reportInfo) {
        Query query = new Query(Criteria.where(INTERVIEW_ID).is(reportInfo.getInterviewId()));
        return mongoTemplate.findAndReplace(query, reportInfo);
    }

    public ReportData getReportDataByJdId(String jdId){
        Query query = new Query(Criteria.where("jdId").is(jdId));
        return mongoTemplate.findOne(query, ReportData.class);
    }


    public ReportData getReportInfo(String interviewId){
        Query query = new Query(Criteria.where(INTERVIEW_ID).is(interviewId));
        return mongoTemplate.findOne(query, ReportData.class);
    }

    public boolean deleteReportInfo(String interviewId){
        Query query = new Query(Criteria.where(INTERVIEW_ID).is(interviewId));
        DeleteResult result=mongoTemplate.remove(query, ReportData.class);
        return result.wasAcknowledged();
    }
}
