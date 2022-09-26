/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.service.impl;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hireplusplus.interviewservice.controller.client.AccountManagerServiceClient;
import com.hireplusplus.interviewservice.controller.client.AiServiceClient;
import com.hireplusplus.interviewservice.controller.client.CommunicationServiceClient;
import com.hireplusplus.interviewservice.controller.client.SchedulerServiceClient;
import com.hireplusplus.interviewservice.enums.Event;
import com.hireplusplus.interviewservice.enums.ResultCode;
import com.hireplusplus.interviewservice.models.api.*;
import com.hireplusplus.interviewservice.models.db.InterviewMediaFilesData;
import com.hireplusplus.interviewservice.models.db.Question;
import com.hireplusplus.interviewservice.models.db.ReportData;
import com.hireplusplus.interviewservice.models.db.StandAloneInterviewData;
import com.hireplusplus.interviewservice.repository.InterviewMediaFilesRepository;
import com.hireplusplus.interviewservice.repository.QuestionnaireRepository;
import com.hireplusplus.interviewservice.repository.ReportRepository;
import com.hireplusplus.interviewservice.repository.StandAloneInterviewRepository;
import com.hireplusplus.interviewservice.service.ReportApiHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   19-07-2022
 ** @ClassName     :    ReportApiHandlerImpl
 ** @Summary       :
 ****************************************************************************************/
@Service
@Slf4j
public class ReportApiHandlerImpl implements ReportApiHandler {

    @Autowired
    private ReportRepository reportRepo;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private AccountManagerServiceClient accountManagerServiceClient;
    @Autowired
    private SchedulerServiceClient schedulerServiceClient;
    @Autowired
    private AiServiceClient aiServiceClient;
    @Autowired
    private InterviewMediaFilesRepository interviewRepo;
    @Autowired
    private QuestionnaireRepository questionnaireRepo;
    @Autowired
    private CloudStorageHandlerAws cloudStorageHandlerAws;
    @Value("${baseUrl}")
    private String baseUrl;
    @Value("${interviewUiBaseUrl}")
    private String interviewUiBaseUrl;
    @Autowired
    private CommunicationServiceClient communicationServiceClient;
    @Autowired
    private StandAloneInterviewRepository standAloneInterviewRepository;

    @Override
    public String generateReportHtml(GenerateReportRequestBody generateReportRequestBody) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        double finalScore = calculateFinalScore(generateReportRequestBody.getSkillAssessmentInfo());
        Context reportGenerationContext = new Context();
        reportGenerationContext.setVariable("id", generateReportRequestBody.getInterviewId());
        reportGenerationContext.setVariable("finalScore", finalScore);
        reportGenerationContext.setVariable("subSkillInfoList", generateReportRequestBody.getSkillAssessmentInfo().getSubSkillAssessmentInfoList());
        reportGenerationContext.setVariable("softSkillInfoList", generateReportRequestBody.getSkillAssessmentInfo().getSoftSkillAssessmentInfoList());
        reportGenerationContext.setVariable("communicationCategory", generateReportRequestBody.getSkillAssessmentInfo().getCommunicationCategory());
        reportGenerationContext.setVariable("overallRemarksInfo", generateReportRequestBody.getOverallRemarksInfo());
        reportGenerationContext.setVariable("checkboxval", false);
        String reportHtmlString = templateEngine.process("report/report", reportGenerationContext);
//        apiResponse.setData(reportHtmlString);
//        log.info("Report Html : {}",reportHtmlString);
        return reportHtmlString;

    }

    private double calculateFinalScore(SkillAssessmentInfo skillAssessmentInfo) {
        double finalScore = 0;
        for (SubSkillAssessmentInfo subSkill : skillAssessmentInfo.getSubSkillAssessmentInfoList()) {
            finalScore += subSkill.getSkillRating() / skillAssessmentInfo.getSubSkillAssessmentInfoList().size();
        }
        log.info("finalScore : " + finalScore);
        finalScore = Math.round(finalScore * 100) / 100.0;
        log.info("finalScore : " + finalScore);
        return finalScore;
    }

    @Override
    public String getSampleReportHtml() {
        Context sampleContext = new Context();
        String reportHtmlString = templateEngine.process("test/sample", sampleContext);
        return reportHtmlString;
    }

    @Override
    public HirePlusPlusResponseBody getCompleteInterviewData(String slotId) {

        if (slotId.contains("STANDALONE_")) {
            return getStandAloneInterviewData(slotId);

        }
        //check flag standalone and call a new method to populated interview init data

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        InterviewInitData interviewInitData = new InterviewInitData();

        List<UserInfo> usersInfo = new ArrayList<>();
        SlotApiData slotApiData = null;
        //slotstatus check, time check can also be done here and allow the user to join
        try {
            HirePlusPlusResponseBody slotApiResponse = schedulerServiceClient.getSlotData(slotId);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(slotApiResponse.getData());
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.findAndRegisterModules();
            slotApiData = objMapper.readValue(json, SlotApiData.class);
            if (slotApiData == null) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No slot found with slotId : " + slotId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            log.info(slotApiData.toString());

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode(ResultCode.Failure.name());
            resultStatusInfo.setMessage("Failed to get information from scheduler  regarding  slot details");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        try {
            List<String> userIdList = new ArrayList<>();
            userIdList.add(slotApiData.getPanelistId());
            userIdList.add(slotApiData.getCandidateId());
            userIdList.add(slotApiData.getRecruiterId());
            HirePlusPlusResponseBody usersInfoResponse = accountManagerServiceClient.getUsersDetails(userIdList);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(usersInfoResponse.getData());
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.findAndRegisterModules();
            usersInfo = Arrays.asList(objMapper.readValue(json, UserInfo[].class));

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode(ResultCode.Failure.name());
            resultStatusInfo.setMessage("Failed to get information from accountManager  to populate user details");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        JobDescriptionData jdData = populateJdData(slotApiData.getJdId());
        if (jdData == null) {
            resultStatusInfo.setResultCode(ResultCode.Failure.name());
            resultStatusInfo.setMessage("Failed to get information from aiservice  to populate skill details");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        interviewInitData.setJdData(jdData);
        interviewInitData.setCandidateReviewInfo(populateCandidateReviewInfo(slotApiData.getJdId(), slotApiData.getCandidateId(), slotApiData.getRecruiterId()));
        interviewInitData.setInterviewInfo(populateInterviewInfo(slotApiData));
        interviewInitData.setUserInfoList(usersInfo);


        InterviewMediaFilesData interviewRepoData = interviewRepo.getInterviewMediaFilesInfoById(slotId);
        if (interviewRepoData == null) {
            interviewRepoData = new InterviewMediaFilesData();
            interviewRepoData.setInterviewId(slotId);
            interviewRepo.saveInterviewCompleteInfo(interviewRepoData);
        }

        ReportData reportRepoData = reportRepo.getReportInfo(slotId);

        if (reportRepoData == null) {
            reportRepoData = new ReportData();
            reportRepoData.setInterviewId(slotId);
            reportRepoData.setJdId(jdData.getJdIdentifier());
            reportRepoData.setResumeId(slotApiData.getCandidateId());
            String objectKey = slotId + "/video/" + slotId + ".webm";
            apiResponse = cloudStorageHandlerAws.getPreSignedUrl(objectKey, 7 * 24);
            if (apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
                reportRepoData.setRecordedVideoUrl((String) apiResponse.getData());
            }
            reportRepo.saveReport(reportRepoData);
        }
        interviewInitData.setRecordedVideoUrl(reportRepoData.getRecordedVideoUrl());

        apiResponse.setData(interviewInitData);
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Data retrieval successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }


    public CandidateReviewInfo populateCandidateReviewInfo(String jdId, String candidateId, String recruiterId) {
        CandidateReviewInfo candidateReviewInfo;
        try {
            HirePlusPlusResponseBody candidateReviewInfoResponse = aiServiceClient.getCandidateReviewInfo(recruiterId, jdId, candidateId);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            if (candidateReviewInfoResponse.getData() != null) {

                String json = ow.writeValueAsString(candidateReviewInfoResponse.getData());
                ObjectMapper objMapper = new ObjectMapper();
                objMapper.findAndRegisterModules();
                candidateReviewInfo = objMapper.readValue(json, CandidateReviewInfo.class);
                candidateReviewInfo.setResumeState(null);
            } else {
                candidateReviewInfo = new CandidateReviewInfo();
                candidateReviewInfo.setCity("NA");
                candidateReviewInfo.setDomainExperience(new ArrayList<>());
                candidateReviewInfo.setPastEmployers(new PastEmployers());
                candidateReviewInfo.setTotalExperience("0");
                candidateReviewInfo.setRelevantExperience("0");

            }
            return candidateReviewInfo;


        } catch (Exception e) {
            e.printStackTrace();
            log.error("Not able to get candidate review info from aiservice continuing with dummy info");
            candidateReviewInfo = new CandidateReviewInfo();
            candidateReviewInfo.setCity("NA");
            candidateReviewInfo.setDomainExperience(new ArrayList<>());
            candidateReviewInfo.setPastEmployers(new PastEmployers());
            candidateReviewInfo.setTotalExperience("0");
            candidateReviewInfo.setRelevantExperience("0");
            return candidateReviewInfo;
        }
    }


    private InterviewInfo populateInterviewInfo(SlotApiData slotApiData) {
        InterviewInfo interviewInfo = new InterviewInfo();
        interviewInfo.setInterviewDate(slotApiData.getStartTime().toLocalDate().toString());
        interviewInfo.setInterviewStartTime(slotApiData.getStartTime().toLocalTime().format(DateTimeFormatter.ISO_TIME));
        interviewInfo.setInterviewEndTime(slotApiData.getEndTime().toLocalTime().format(DateTimeFormatter.ISO_TIME));
        interviewInfo.setTimeZone("UTC/GMT");
        return interviewInfo;
    }


    public JobDescriptionData populateJdData(String jdId) {
        JdDataForInterview jdDataForInterview;
        try {
            HirePlusPlusResponseBody jdDataForInterviewResponse = aiServiceClient.getJdDataForInterview(jdId);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            if (jdDataForInterviewResponse.getData() != null) {

                String json = ow.writeValueAsString(jdDataForInterviewResponse.getData());
                ObjectMapper objMapper = new ObjectMapper();
                objMapper.findAndRegisterModules();
                jdDataForInterview = objMapper.readValue(json, JdDataForInterview.class);

                log.info(jdDataForInterview.toString());

                JobDescriptionData jdData = new JobDescriptionData();
                jdData.setJdIdentifier(jdDataForInterview.getJdId());
                jdData.setJdTitle(jdDataForInterview.getJdTitle());
                List<InterviewSkillItem> skillsList = jdDataForInterview.getJdSkillDataList();
                List<SkillData> skillDataList = new ArrayList<>();

                //Java,MongoDB,Angular, Spring boot
                for (InterviewSkillItem skill : skillsList) {
                    SkillData skillData = new SkillData();
                    skillData.setSkillName(skill.getSkillName());
                    skillData.setSkillWeightage(skill.getSkillWeightage());
                    List<SuggestedQuestion> suggestedQuestionList = new ArrayList<>();
                    List<Question> questionsRepoList = questionnaireRepo.getQuestionsBySkillName(skill.getSkillName(), 24);
                    int j = 0;
                    for (Question question : questionsRepoList) {
                        SuggestedQuestion suggestedQuestion = new SuggestedQuestion();
                        suggestedQuestion.setQuestion(question.getQuestion());
                        ///random difficulty level
                        if (j % 3 == 0) {

                            suggestedQuestion.setDifficultyLevel("Easy");

                        } else if (j % 3 == 1) {
                            suggestedQuestion.setDifficultyLevel("Medium");

                        } else {
                            suggestedQuestion.setDifficultyLevel("Difficult");

                        }
                        j++;
                        /////////
//           suggestedQuestion.setDifficultyLevel(question.getDifficultyLevel());
                        suggestedQuestion.setModelAnswer(question.getAnswer());
                        suggestedQuestionList.add(suggestedQuestion);
                    }

                    skillData.setSuggestedQuestionList(suggestedQuestionList);
                    skillDataList.add(skillData);


                }

                jdData.setSkillList(skillDataList);
                return jdData;
            } else {

                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Not able to get jd data from  aiservice . cannot populate skills");
            return null;
        }

    }

    @Override
    public HirePlusPlusResponseBody getReportData(String interviewId) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        ReportData reportRepoData = reportRepo.getReportInfo(interviewId);
        if (reportRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No report data found with interviewId : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        apiResponse.setData(reportRepoData);
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Retrieval successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    @Override
    public HirePlusPlusResponseBody updateReportData(ReportData reportData) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        ReportData reportRepoData = reportRepo.getReportInfo(reportData.getInterviewId());
        if (reportRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No report data found with interviewId : " + reportData.getInterviewId());
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        reportRepoData = reportRepo.updateReport(reportData);
        if (reportRepoData != null) {
            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage("report updated successfully");
            apiResponse.setData(reportData);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        resultStatusInfo.setResultCode(ResultCode.Failure.name());
        resultStatusInfo.setMessage("report update failed at db level");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    @Override
    public HirePlusPlusResponseBody streamCode(String interviewId, HttpServletResponse servletResponse) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        ReportData reportRepoData = reportRepo.getReportInfo(interviewId);
        if (reportRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No report data found with interviewId : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        try {
            String codeString = "==================================================================================================================" +
                    "\nCode : \n" + "==================================================================================================================" +
                    "\n\n" + (reportRepoData.getCodeString() == null ? "No Code Data available" : reportRepoData.getCodeString()) + "\n\n";
            String compilationString = "\n==================================================================================================================" +
                    "\nCompilation Info : \n" + "==================================================================================================================" +
                    "\n\n" + (reportRepoData.getCompilationString() == null ? "No Code Data available" : reportRepoData.getCompilationString());
            ServletOutputStream stream = servletResponse.getOutputStream();
            stream.write(codeString.getBytes(StandardCharsets.UTF_8));
            stream.write(compilationString.getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode(ResultCode.Failure.name());
            resultStatusInfo.setMessage("failed to stream code");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("code stream successfully");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;

    }

    @Override
    public HirePlusPlusResponseBody saveCode(String interviewId, CodeDataRequestBody codeDataRequestBody) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        ReportData reportRepoData = reportRepo.getReportInfo(interviewId);
        if (reportRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No report data found with interviewId : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        reportRepoData.setCodeString(codeDataRequestBody.getCodeString());
        reportRepoData.setCompilationString(codeDataRequestBody.getCompilationString());
        reportRepoData = reportRepo.updateReport(reportRepoData);

        if (reportRepoData != null) {
            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage("code updated successfully");
            apiResponse.setData(codeDataRequestBody);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        resultStatusInfo.setResultCode(ResultCode.Failure.name());
        resultStatusInfo.setMessage("report update failed at db level");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    @Override
    public HirePlusPlusResponseBody createStandAloneInterviewRoom(MultipartFile resumeFile, MultipartFile jdFile, StandAloneRequestBody standAloneRequestBody) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        if (resumeFile == null) {
            resultStatusInfo.setResultCode(ResultCode.CorruptFile.name());
            resultStatusInfo.setMessage("Resume file is null or empty");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        if (jdFile == null) {
            resultStatusInfo.setResultCode(ResultCode.CorruptFile.name());
            resultStatusInfo.setMessage("JD file is null or empty");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        try {
            byte[] resumeByteArray = resumeFile.getBytes();
            byte[] jdByteArray = jdFile.getBytes();

            if (resumeByteArray == null || resumeByteArray.length == 0) {
                resultStatusInfo.setResultCode(ResultCode.CorruptFile.name());
                resultStatusInfo.setMessage("Resume file is null or empty");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            if (jdByteArray == null || jdByteArray.length == 0) {
                resultStatusInfo.setResultCode(ResultCode.CorruptFile.name());
                resultStatusInfo.setMessage("JD file is null or empty");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }


        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode(ResultCode.CorruptFile.name());
            resultStatusInfo.setMessage("Failed to parse JD/Resume to byte array");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        if (standAloneRequestBody.getUserInfoList() == null || standAloneRequestBody.getUserInfoList().size() < 2) {
            resultStatusInfo.setResultCode("INSUFFICIENT_USERS");
            resultStatusInfo.setMessage("Minimum two people are required to create an  interview room");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        if (standAloneRequestBody.getInterviewInfo() == null) {
            resultStatusInfo.setResultCode("INSUFFICIENT_DATA");
            resultStatusInfo.setMessage("Interview Info not found");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        //check interview date and time and allow only if slot is not past
        InterviewInfo interviewInfo= standAloneRequestBody.getInterviewInfo();

//        cloudStorageHandlerAws.uploadFileToCloudStorage(resumeFile.getInputStream(),"f","filename",interviewId)
        String epoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)+"";
        String jdTitle = standAloneRequestBody.getJdTitle();
        String clientName = standAloneRequestBody.getClientName();
        String interviewId = "STANDALONE_" + clientName.substring(0, 2).toUpperCase() + "_" + jdTitle.substring(0, 2).toUpperCase() + epoch;
        String jdId = "SA_JD" + epoch;
        String jdExtension = FilenameUtils.getExtension(jdFile.getOriginalFilename());
        String resumeId = "SA_CV" +epoch;
        String resumeExtension = FilenameUtils.getExtension(resumeFile.getOriginalFilename());
        String folderName = interviewId + "/files";

        try {
            apiResponse = cloudStorageHandlerAws.uploadFileToCloudStorage(resumeFile.getInputStream(), folderName, resumeId + "." + resumeExtension, interviewId);
            if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
                resultStatusInfo.setResultCode(apiResponse.getResultStatusInfo().getResultCode());
                resultStatusInfo.setMessage("Failed to upload resume in cloud for standalone interview Id : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                apiResponse.setData(null);
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("FILE_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("Failed to upload resume in cloud for standalone interview Id : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            apiResponse.setData(null);
            return apiResponse;
        }

        try {
            apiResponse = cloudStorageHandlerAws.uploadFileToCloudStorage(jdFile.getInputStream(), folderName, jdId + "." + jdExtension, interviewId);
            if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
                resultStatusInfo.setResultCode(apiResponse.getResultStatusInfo().getResultCode());
                resultStatusInfo.setMessage("Failed to upload Jd in cloud for standalone interview Id : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                apiResponse.setData(null);
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("FILE_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("Failed to upload jd in cloud for standalone interview Id : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            apiResponse.setData(null);
            return apiResponse;
        }
        List<StandAloneUserInfo> standAloneUserInfoList = standAloneRequestBody.getUserInfoList();
        List<UserInfo> userInfoList = new ArrayList<>();
        for (StandAloneUserInfo user : standAloneUserInfoList) {
            UserInfo userInfo = new UserInfo();
            userInfo.setEmail(user.getEmail());
            userInfo.setUserRole(user.getUserRole());
            userInfo.setFirstName(user.getFirstName());
            userInfo.setLastName(user.getLastName());
            userInfo.setMobile(user.getMobile());
            if(userInfo.getUserRole().equalsIgnoreCase("CANDIDATE")){
                userInfo.setUserIdentifier(resumeId);

            }
            userInfoList.add(userInfo);
        }

        List<SkillData> skillDataList = standAloneRequestBody.getJdSkillDataList();
        for (SkillData skill : skillDataList) {
            List<SuggestedQuestion> questionList = skill.getSuggestedQuestionList();
            if(questionList==null)
                questionList=new ArrayList<>();

            if (questionList.size() <= 24) {
                List<Question> questionsRepoList = questionnaireRepo.getQuestionsBySkillName(skill.getSkillName(), 24 - questionList.size());
                int j = 0;
                for (Question question : questionsRepoList) {
                    SuggestedQuestion suggestedQuestion = new SuggestedQuestion();
                    suggestedQuestion.setQuestion(question.getQuestion());
                    ///random difficulty level
                    if (j % 3 == 0) {
                        suggestedQuestion.setDifficultyLevel("Easy");
                    } else if (j % 3 == 1) {
                        suggestedQuestion.setDifficultyLevel("Medium");
                    } else {
                        suggestedQuestion.setDifficultyLevel("Difficult");
                    }
                    j++;
                    suggestedQuestion.setModelAnswer(question.getAnswer());
                    questionList.add(suggestedQuestion);
                }
            }
            skill.setSuggestedQuestionList(questionList);
        }


        String jdUrl = baseUrl + "/report/standAlone/jd?jdId=" + jdId;
        String resumeUrl = baseUrl + "/report/standAlone/resume?jdId=" + jdId+"&resumeId="+resumeId;

        StandAloneInterviewData standAloneInterviewData = new StandAloneInterviewData();
        standAloneInterviewData.setInterviewId(interviewId);
        standAloneInterviewData.setInterviewInfo(standAloneRequestBody.getInterviewInfo());
        standAloneInterviewData.setJdId(jdId);
        standAloneInterviewData.setJdTitle(standAloneRequestBody.getJdTitle());
        standAloneInterviewData.setJdUrl(jdUrl);
        standAloneInterviewData.setJdExtension(jdExtension);
        standAloneInterviewData.setResumeId(resumeId);
        standAloneInterviewData.setResumeUrl(resumeUrl);
        standAloneInterviewData.setResumeExtension(resumeExtension);
        standAloneInterviewData.setUserInfoList(userInfoList);
        standAloneInterviewData.setSkillDataList(skillDataList);

        standAloneInterviewData = standAloneInterviewRepository.saveStandAloneInterviewData(standAloneInterviewData);
        if (standAloneInterviewData == null) {
            resultStatusInfo.setResultCode(ResultCode.DbFailure.name());
            resultStatusInfo.setMessage("Failed to insert standalone interview data in to db");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        UserInfo accManagerInfo = null;
        UserInfo candidateInfo = null;
        UserInfo panelistInfo = null;



        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getUserRole().equalsIgnoreCase("CANDIDATE"))
                candidateInfo = userInfo;
            else if (userInfo.getUserRole().equalsIgnoreCase("PANELIST"))
                panelistInfo = userInfo;
            else if (userInfo.getUserRole().equalsIgnoreCase("RECRUITER"))
                accManagerInfo = userInfo;
        }
        UserInfo finalAccManagerInfo = accManagerInfo;
        UserInfo finalCandidateInfo = candidateInfo;
        UserInfo finalPanelistInfo = panelistInfo;
        try {
            //send emails to concerned parties
            userInfoList.forEach((userInfo -> {
                if (userInfo.getUserRole().equalsIgnoreCase("CANDIDATE")) {
                    SendEmailRequestBody emailRequestBody = new SendEmailRequestBody();
                    emailRequestBody.setEventType(Event.INTERVIEW_SCHEDULED_CANDIDATE);
                    emailRequestBody.setPriority(1);
                    emailRequestBody.setToAddress(userInfo.getEmail());
                    Map<String, String> data = new HashMap<>();
                    data.put("fullName", userInfo.getFirstName());
                    String bookingDetails = "Start Time : " + interviewInfo.getInterviewStartTime() + "\n End Time : " + interviewInfo.getInterviewEndTime() + "\n Time Zone: " + interviewInfo.getTimeZone();
                    data.put("bookingDetails", bookingDetails);
                    data.put("jobTitle", jdTitle);
                    data.put("companyName", clientName);
                    data.put("jdUrl", jdUrl);
                    data.put("interviewUrl", interviewUiBaseUrl+"system-checks?interviewId=" + interviewId + "&userRole=" + userInfo.getUserRole() + "&userName=" + userInfo.getFirstName());

                    data.put("accountManagerName", finalAccManagerInfo.getFirstName() + " " + finalAccManagerInfo.getLastName());
                    data.put("accountManagerEmail", finalAccManagerInfo.getEmail());
                    data.put("accountManagerMobile", finalAccManagerInfo.getMobile());
                    emailRequestBody.setData(data);
                    communicationServiceClient.sendEmail(emailRequestBody);
                } else if (userInfo.getUserRole().equalsIgnoreCase("PANELIST")) {
                    SendEmailRequestBody emailRequestBody = new SendEmailRequestBody();
                    emailRequestBody.setEventType(Event.INTERVIEW_SCHEDULED_PANELIST);
                    emailRequestBody.setPriority(1);
                    emailRequestBody.setToAddress(userInfo.getEmail());
                    Map<String, String> data = new HashMap<>();
                    data.put("fullName", userInfo.getFirstName() + " " + userInfo.getLastName());
                    String bookingDetails = "Start Time : " + interviewInfo.getInterviewStartTime() + "\n End Time : " + interviewInfo.getInterviewEndTime() + "\n Time Zone: " + interviewInfo.getTimeZone();
                    data.put("bookingDetails", bookingDetails);
                    data.put("jobTitle", jdTitle);
                    data.put("companyName", clientName);
                    data.put("jdUrl", jdUrl);
                    data.put("resumeUrl", resumeUrl);
                    data.put("interviewUrl", interviewUiBaseUrl+"system-checks?interviewId=" + interviewId + "&userRole=" + userInfo.getUserRole() + "&userName=" + userInfo.getFirstName());
                    data.put("accountManagerName", finalAccManagerInfo.getFirstName() + " " + finalAccManagerInfo.getLastName());
                    data.put("accountManagerEmail", finalAccManagerInfo.getEmail());
                    data.put("accountManagerMobile", finalAccManagerInfo.getMobile());
                    emailRequestBody.setData(data);
                    communicationServiceClient.sendEmail(emailRequestBody);
                } else if (userInfo.getUserRole().equalsIgnoreCase("RECRUITER")) {
                    SendEmailRequestBody emailRequestBody = new SendEmailRequestBody();
                    emailRequestBody.setEventType(Event.INTERVIEW_SCHEDULED_RECRUITER);
                    emailRequestBody.setPriority(1);
                    emailRequestBody.setToAddress(userInfo.getEmail());
                    Map<String, String> data = new HashMap<>();
                    data.put("fullName", userInfo.getFirstName() + " " + userInfo.getLastName());
                    String bookingDetails = "Start Time : " + interviewInfo.getInterviewStartTime() + "\n End Time : " + interviewInfo.getInterviewEndTime() + "\n Time Zone: " + interviewInfo.getTimeZone();
                    data.put("bookingDetails", bookingDetails);
                    data.put("jobTitle", jdTitle);
                    data.put("companyName", clientName);
                    data.put("jdLocation", jdUrl);
                    data.put("resumeLocation", resumeUrl);
                    data.put("interviewUrl", interviewUiBaseUrl+"system-checks?interviewId=" + interviewId + "&userRole=" + userInfo.getUserRole() + "&userName=" + userInfo.getFirstName());
                    data.put("candidateName", finalCandidateInfo.getFirstName() + " " + finalCandidateInfo.getLastName());
                    data.put("candidateEmail", finalCandidateInfo.getEmail());
                    data.put("candidateMobile", finalCandidateInfo.getMobile());

                    data.put("panelistName", finalPanelistInfo.getFirstName() + " " + finalPanelistInfo.getLastName());
                    data.put("panelistEmail", finalPanelistInfo.getEmail());
                    data.put("panelistMobile", finalPanelistInfo.getMobile());
                    emailRequestBody.setData(data);
                    communicationServiceClient.sendEmail(emailRequestBody);
                }
            }));
        }catch (Exception e){
            e.printStackTrace();
            resultStatusInfo.setResultCode(ResultCode.Failure.name());
            resultStatusInfo.setMessage("Interview Room created but Exception in communicating with communication service. Failed to send emails.");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        //email end
        apiResponse.setData(standAloneInterviewData);
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("StandAlone Interview Room has been created successfully with ID : " + interviewId);
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }


    private HirePlusPlusResponseBody getStandAloneInterviewData(String slotId) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        StandAloneInterviewData standAloneInterviewData = standAloneInterviewRepository.getStandAloneInterviewData(slotId);
        if (standAloneInterviewData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No Data found with interviewId: " + slotId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }

        InterviewMediaFilesData interviewMediaFilesData = interviewRepo.getInterviewMediaFilesInfoById(slotId);
        if (interviewMediaFilesData == null) {
            interviewMediaFilesData = new InterviewMediaFilesData();
            interviewMediaFilesData.setInterviewId(slotId);
            interviewRepo.saveInterviewCompleteInfo(interviewMediaFilesData);
        }

        ReportData reportRepoData = reportRepo.getReportInfo(slotId);

        if (reportRepoData == null) {
            reportRepoData = new ReportData();
            reportRepoData.setInterviewId(slotId);
            reportRepoData.setStandaloneInterview(true);
            reportRepoData.setJdId(standAloneInterviewData.getJdId());
            reportRepoData.setResumeId(standAloneInterviewData.getResumeId());
            String objectKey = slotId + "/video/" + slotId + ".webm";
            apiResponse = cloudStorageHandlerAws.getPreSignedUrl(objectKey, 7 * 24);
            if (apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
                reportRepoData.setRecordedVideoUrl((String) apiResponse.getData());
            }
            reportRepo.saveReport(reportRepoData);
        }



        InterviewInitData interviewInitData = new InterviewInitData();
        interviewInitData.setStandaloneInterview(true);
        //jdData
        JobDescriptionData jdData = new JobDescriptionData();
        jdData.setJdTitle(standAloneInterviewData.getJdTitle());
        jdData.setJdIdentifier(standAloneInterviewData.getJdId());
        jdData.setSkillList(standAloneInterviewData.getSkillDataList());

        InterviewInfo interviewInfo = standAloneInterviewData.getInterviewInfo();

        List<UserInfo> userInfoList = standAloneInterviewData.getUserInfoList();


        //NOT Required
//        CandidateReviewInfo candidateReviewInfo = new CandidateReviewInfo();

        String resumeUrl = standAloneInterviewData.getResumeUrl();
        String jdUrl = standAloneInterviewData.getJdUrl();
        String recordedVideoUrl = reportRepoData.getRecordedVideoUrl();

        interviewInitData.setJdData(jdData);
        interviewInitData.setInterviewInfo(interviewInfo);
        interviewInitData.setUserInfoList(userInfoList);
        interviewInitData.setResumeUrl(resumeUrl);
        interviewInitData.setJdUrl(jdUrl);
        interviewInitData.setRecordedVideoUrl(recordedVideoUrl);

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Retrieval successful");

        apiResponse.setData(interviewInitData);
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    @Override
    public HirePlusPlusResponseBody downloadStandAloneResume(String jdId, String resumeId, HttpServletResponse servletResponse) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        StandAloneInterviewData standAloneInterviewData = standAloneInterviewRepository.getStandAloneInterviewDataByJdId(jdId);
        if (standAloneInterviewData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No Data found with jdId : " + jdId+" resumeId : "+resumeId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        String interviewId = standAloneInterviewData.getInterviewId();
        String folderName = interviewId + "/files";
        String fileName = standAloneInterviewData.getResumeId()+"." + standAloneInterviewData.getResumeExtension();
        apiResponse = cloudStorageHandlerAws.downloadFilesFromCloudStorage(folderName, Arrays.asList(fileName), interviewId);
        if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
            resultStatusInfo.setResultCode(apiResponse.getResultStatusInfo().getResultCode());
            resultStatusInfo.setMessage("Failed to download resume from the cloud for standalone interview Id " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            apiResponse.setData(null);
            return apiResponse;
        }

        List<byte[]> downloadedFilesList = (List<byte[]>) apiResponse.getData();
        if (downloadedFilesList.size() == 0) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No resume file  found in cloud for interviewId : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        try {
            ServletOutputStream stream = servletResponse.getOutputStream();
            if (standAloneInterviewData.getResumeExtension().equalsIgnoreCase("docx")
                    || standAloneInterviewData.getResumeExtension().equalsIgnoreCase("doc")) {
                servletResponse.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }

            stream.write(downloadedFilesList.get(0));
            stream.flush();
            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("STREAMING_EXCEPTION");
            resultStatusInfo.setMessage("Failed to stream file to the browser");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Stream successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    @Override
    public HirePlusPlusResponseBody downloadStandAloneJd(String jdId, HttpServletResponse servletResponse) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        StandAloneInterviewData standAloneInterviewData = standAloneInterviewRepository.getStandAloneInterviewDataByJdId(jdId);
        if (standAloneInterviewData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No Data found with jdId : " + jdId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        String interviewId = standAloneInterviewData.getInterviewId();
        String folderName = interviewId + "/files";
        String fileName = standAloneInterviewData.getJdId()+"." + standAloneInterviewData.getResumeExtension();
        apiResponse = cloudStorageHandlerAws.downloadFilesFromCloudStorage(folderName, Arrays.asList(fileName), interviewId);
        if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
            resultStatusInfo.setResultCode(apiResponse.getResultStatusInfo().getResultCode());
            resultStatusInfo.setMessage("Failed to download resume from the cloud for standalone interview Id " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<byte[]> downloadedFilesList = (List<byte[]>) apiResponse.getData();
        if (downloadedFilesList.size() == 0) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No resume file  found in cloud for interviewId : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        try {
            ServletOutputStream stream = servletResponse.getOutputStream();
            if (standAloneInterviewData.getResumeExtension().equalsIgnoreCase("docx")
                    || standAloneInterviewData.getResumeExtension().equalsIgnoreCase("doc")) {
                servletResponse.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }
            stream.write(downloadedFilesList.get(0));
            stream.flush();
            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("STREAMING_EXCEPTION");
            resultStatusInfo.setMessage("Failed to stream file to the browser");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("Stream successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }
}
