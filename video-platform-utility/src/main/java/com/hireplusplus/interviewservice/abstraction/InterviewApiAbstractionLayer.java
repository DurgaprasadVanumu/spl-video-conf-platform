/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.abstraction;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hireplusplus.interviewservice.controller.client.AiServiceClient;
import com.hireplusplus.interviewservice.enums.ResultCode;
import com.hireplusplus.interviewservice.enums.ResumeState;
import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import com.hireplusplus.interviewservice.models.api.ResultStatusInfo;
import com.hireplusplus.interviewservice.models.api.UpdateResumeStateRequestBody;
import com.hireplusplus.interviewservice.models.db.InterviewMediaFilesData;
import com.hireplusplus.interviewservice.models.db.ReportData;
import com.hireplusplus.interviewservice.repository.InterviewMediaFilesRepository;
import com.hireplusplus.interviewservice.repository.ReportRepository;
import com.hireplusplus.interviewservice.service.impl.CloudStorageHandlerAws;
import com.hireplusplus.interviewservice.service.impl.CloudStorageHandlerAzure;
import com.hireplusplus.interviewservice.utils.MemoryFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   10-08-2022
 ** @ClassName     :    InterviewApiAbstractionLayer
 ** @Summary       :
 ****************************************************************************************/
@Service
@Slf4j
public class InterviewApiAbstractionLayer {
    @Autowired
    private CloudStorageHandlerAzure cloudStorageHandlerAzure;
    @Value("${interview.containerName}")
    private String interviewContainer;
    @Value("${connectionString}")
    private String connectionString;
    @Autowired
    private InterviewMediaFilesRepository interviewRepo;
    @Value("${storageAccount}")
    private String storageAccount;
    @Autowired
    private CloudStorageHandlerAws cloudStorageHandlerAws;
    private final String AWS = "aws";
    private final String AZURE = "azure";
    @Autowired
    private AiServiceClient aiServiceClient;
    @Autowired
    private ReportRepository reportRepo;

    public HirePlusPlusResponseBody endInterview(String interviewId) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        ReportData reportRepoData = reportRepo.getReportInfo(interviewId);
        if (reportRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("No Data found with interviewId : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }

        ///Resume State change
        if (!reportRepoData.isStandaloneInterview()) {
            try {
                UpdateResumeStateRequestBody updateResumeStateRequestBody = new UpdateResumeStateRequestBody();
                updateResumeStateRequestBody.setResumeIdentifier(reportRepoData.getResumeId());
                updateResumeStateRequestBody.setJdIdentifier(reportRepoData.getJdId());
                updateResumeStateRequestBody.setResumeState(ResumeState.INTERVIEW_SCHEDULED);
                HirePlusPlusResponseBody jobResponse = aiServiceClient.updateResumeState(updateResumeStateRequestBody);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(jobResponse.getResultStatusInfo());
                ObjectMapper objMapper = new ObjectMapper();
                objMapper.findAndRegisterModules();
                resultStatusInfo = objMapper.readValue(json, ResultStatusInfo.class);
                log.info(resultStatusInfo.toString());

                if (!resultStatusInfo.getResultCode().equalsIgnoreCase("SUCCESS")) {
                    resultStatusInfo.setResultCode(ResultCode.Failure.name());
                    resultStatusInfo.setMessage("Failed to update resume state to" + ResumeState.INTERVIEW_COMPLETED + " in aiservice  for resumeId " + reportRepoData.getResumeId() + "jdId " + reportRepoData.getJdId());
                    apiResponse.setResultStatusInfo(resultStatusInfo);
                }
                log.info(resultStatusInfo.toString());
            } catch (Exception e) {
                e.printStackTrace();
                resultStatusInfo.setResultCode(ResultCode.Failure.name());
                resultStatusInfo.setMessage("Feign exception. Failed to get update resume state to" + ResumeState.INTERVIEW_COMPLETED + " in aiservice  for resumeId " + reportRepoData.getResumeId() + "jdId " + reportRepoData.getJdId());
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
        }
        if (storageAccount.equalsIgnoreCase(AWS)) {
            return postProcessVideoAws(interviewId);

        } else {
            return postProcessVideo(interviewId);

        }


    }

    public HirePlusPlusResponseBody uploadSnapshot(MultipartFile snapshot, String interviewId) {

        if (storageAccount.equalsIgnoreCase(AWS)) {
            return uploadSnapshotAws(snapshot, interviewId);

        }

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        InterviewMediaFilesData interviewCompleteInfo = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewCompleteInfo == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("InterviewId not found in db : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        int snapshotCount = interviewCompleteInfo.getSnapshotCount();

        try {
            byte[] fileByteArray = snapshot.getBytes();
            String filename = snapshotCount + "." + FilenameUtils.getExtension(snapshot.getOriginalFilename());
            String folderName = interviewId + "/snapshots";
            InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            boolean status = cloudStorageHandlerAzure.uploadFileToCloudStorage(inputStream, folderName, filename, fileByteArray.length, connectionString, interviewContainer);
            if (!status) {
                resultStatusInfo.setResultCode("SNAPSHOT_UPLOAD_FAILED");
                resultStatusInfo.setMessage("snapshot upload to cloud storage failed");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("SNAPSHOT_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("snapshot upload to cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        snapshotCount++;
        interviewCompleteInfo.setSnapshotCount(snapshotCount);
        interviewCompleteInfo.setSnapshotFileNameExtension(FilenameUtils.getExtension(snapshot.getOriginalFilename()));
        interviewRepo.updateInterviewMediaFilesInfo(interviewId, interviewCompleteInfo);

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("snapshot upload to cloud storage successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    private HirePlusPlusResponseBody uploadSnapshotAws(MultipartFile snapshot, String interviewId) {

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        InterviewMediaFilesData interviewMediaFilesInfo = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewMediaFilesInfo == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("InterviewId not found in db : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        int snapshotCount = interviewMediaFilesInfo.getSnapshotCount();

        try {
            byte[] fileByteArray = snapshot.getBytes();
            String filename = snapshotCount + "." + FilenameUtils.getExtension(snapshot.getOriginalFilename());
            String folderName = interviewId + "/snapshots";
            InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            inputStream.close();
            apiResponse = cloudStorageHandlerAws.uploadFileToCloudStorage(inputStream, folderName, filename, interviewId);
            if (apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("SUCCESS")) {
                snapshotCount++;
                interviewMediaFilesInfo.setSnapshotCount(snapshotCount);
                interviewMediaFilesInfo.setSnapshotFileNameExtension(FilenameUtils.getExtension(snapshot.getOriginalFilename()));
                interviewRepo.updateInterviewMediaFilesInfo(interviewId, interviewMediaFilesInfo);

            }

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("SNAPSHOT_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("snapshot upload to aws cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }


        return apiResponse;
    }


    public HirePlusPlusResponseBody uploadVideoPart(MultipartFile videoPart, String interviewId) {

        if (storageAccount.equalsIgnoreCase(AWS)) {
            return uploadVideoPartAws(videoPart, interviewId);

        }
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        InterviewMediaFilesData interviewCompleteInfo = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewCompleteInfo == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("InterviewId not found in db : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        int videoPartsCount = interviewCompleteInfo.getVideoPartsCount();

        try {
            byte[] fileByteArray = videoPart.getBytes();
            String filename = videoPartsCount + "." + FilenameUtils.getExtension(videoPart.getOriginalFilename());
            String folderName = interviewId + "/video";
            InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            boolean status = cloudStorageHandlerAzure.uploadFileToCloudStorage(inputStream, folderName, filename, fileByteArray.length, connectionString, interviewContainer);
            if (!status) {
                resultStatusInfo.setResultCode("SNAPSHOT_UPLOAD_FAILED");
                resultStatusInfo.setMessage("video upload to cloud storage failed");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("SNAPSHOT_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("video upload to cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        videoPartsCount++;
        interviewCompleteInfo.setVideoPartsCount(videoPartsCount);
        interviewCompleteInfo.setVideoFileNameExtension(FilenameUtils.getExtension(videoPart.getOriginalFilename()));
        interviewRepo.updateInterviewMediaFilesInfo(interviewId, interviewCompleteInfo);

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("video upload to cloud storage successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    private HirePlusPlusResponseBody uploadVideoPartAws(MultipartFile videoPart, String interviewId) {

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        InterviewMediaFilesData interviewCompleteInfo = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewCompleteInfo == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("InterviewId not found in db : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        int videoPartsCount = interviewCompleteInfo.getVideoPartsCount();

        try {
            byte[] fileByteArray = videoPart.getBytes();
            String filename = videoPartsCount + "." + FilenameUtils.getExtension(videoPart.getOriginalFilename());
            String folderName = interviewId + "/video";
            InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            apiResponse = cloudStorageHandlerAws.uploadFileToCloudStorage(inputStream, folderName, filename, interviewId);
            if (apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("SUCCESS")) {
                videoPartsCount++;
                interviewCompleteInfo.setVideoPartsCount(videoPartsCount);
                interviewCompleteInfo.setVideoFileNameExtension(FilenameUtils.getExtension(videoPart.getOriginalFilename()));
                interviewRepo.updateInterviewMediaFilesInfo(interviewId, interviewCompleteInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("VIDEO_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("video upload to aws cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        return apiResponse;
    }

    @Async("AzureUploadAsyncExecutor")
    public HirePlusPlusResponseBody postProcessVideo(String interviewId) {

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<MemoryFile> downloadList;

        InterviewMediaFilesData interviewCompleteInfo = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewCompleteInfo == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("InterviewId not found in db : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<String> fileList = new ArrayList<>();
        for (int i = 0; i < interviewCompleteInfo.getVideoPartsCount(); i++) {
            fileList.add(i + "." + interviewCompleteInfo.getVideoFileNameExtension());
        }
        log.info(fileList.toString());
        String folderName = interviewId + "/video";

        try {
            downloadList = cloudStorageHandlerAzure.downloadFiles(folderName, fileList, connectionString, interviewContainer);
            if (downloadList == null || downloadList.isEmpty()) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No videoparts found in cloud for interviewId : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            log.info(downloadList.size() + "");

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("CLOUD_STORAGE_EXCEPTION");
            resultStatusInfo.setMessage("failed to get file from cloud");
            return apiResponse;

        }
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (MemoryFile file1 : downloadList) {
                outputStream.write(file1.getContents());
            }
            outputStream.close();

            byte[] fileByteArray = outputStream.toByteArray();
            String filename = interviewId + "." + interviewCompleteInfo.getVideoFileNameExtension();
            InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            boolean status = cloudStorageHandlerAzure.uploadFileToCloudStorage(inputStream, folderName, filename, fileByteArray.length, connectionString, interviewContainer);
            if (!status) {
                resultStatusInfo.setResultCode("VIDEO_UPLOAD_FAILED");
                resultStatusInfo.setMessage("video upload to cloud storage failed");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("VIDEO_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("video upload to cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        try {
            List<Boolean> statusList = cloudStorageHandlerAzure.deleteFiles(folderName, fileList, connectionString, interviewContainer);
            log.info(statusList.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("VIDEO_PARTS_DELETE_EXCEPTION");
            resultStatusInfo.setMessage("video parts deletion from cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("video upload to cloud storage successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    @Async("AWSUploadAsyncExecutor")
    public HirePlusPlusResponseBody postProcessVideoAws(String interviewId) {

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<byte[]> downloadList = new ArrayList<>();

        InterviewMediaFilesData interviewCompleteInfo = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewCompleteInfo == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("InterviewId not found in db : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<String> fileList = new ArrayList<>();
        for (int i = 0; i < interviewCompleteInfo.getVideoPartsCount(); i++) {
            fileList.add(i + "." + interviewCompleteInfo.getVideoFileNameExtension());
        }
        log.info(fileList.toString());
        String folderName = interviewId + "/video";

        try {
            apiResponse = cloudStorageHandlerAws.downloadFilesFromCloudStorage(folderName, fileList, interviewId);
            if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
                resultStatusInfo.setResultCode(apiResponse.getResultStatusInfo().getResultCode());
                resultStatusInfo.setMessage("failed to download snapshots : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            downloadList = (List<byte[]>) apiResponse.getData();
            if (downloadList == null || downloadList.isEmpty()) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No videoParts found in cloud for interviewId : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            log.info(downloadList.size() + "");

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("CLOUD_STORAGE_EXCEPTION");
            resultStatusInfo.setMessage("failed to get file from cloud");
            return apiResponse;

        }
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (byte[] file : downloadList) {
                outputStream.write(file);

            }
            outputStream.close();


            byte[] fileByteArray = outputStream.toByteArray();
            String filename = interviewId + "." + interviewCompleteInfo.getVideoFileNameExtension();
            InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            inputStream.close();
            apiResponse = cloudStorageHandlerAws.uploadFileToCloudStorage(inputStream, folderName, filename, interviewId);
            if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("SUCCESS")) {
                resultStatusInfo.setResultCode("VIDEO_UPLOAD_FAILED");
                resultStatusInfo.setMessage("video upload to cloud storage failed");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("VIDEO_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("video upload to cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        try {
            apiResponse = cloudStorageHandlerAws.deleteFilesFromCloudStorage(folderName, fileList, interviewId);
            if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("SUCCESS")) {
                apiResponse.setResultStatusInfo(apiResponse.getResultStatusInfo());
                return apiResponse;

            }
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("VIDEO_PARTS_DELETE_EXCEPTION");
            resultStatusInfo.setMessage("video parts deletion from cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("video upload to cloud storage successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        log.info("post processing complete for interviewId : " + interviewId);
        return apiResponse;
    }


    public HirePlusPlusResponseBody downloadSnapshot(String interviewId, HttpServletResponse servletResponse) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<MemoryFile> downloadList;
        MemoryFile file;
        InterviewMediaFilesData interviewRepoData = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("Interview Id not found : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<String> fileList = new ArrayList<>();
        for (int i = 0; i < interviewRepoData.getSnapshotCount(); i++) {
            fileList.add(i + "." + interviewRepoData.getSnapshotFileNameExtension());
        }
        log.info(fileList.toString());
        try {
            String folderName = interviewId + "/snapshots";
            downloadList = cloudStorageHandlerAzure.downloadFiles(folderName, fileList, connectionString, interviewContainer);
            if (downloadList == null || downloadList.isEmpty()) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No snapshots found in cloud for interviewId : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("CLOUD_STORAGE_EXCEPTION");
            resultStatusInfo.setMessage("failed to get file from cloud");
            return apiResponse;

        }
        try {

            ServletOutputStream stream = servletResponse.getOutputStream();
            stream.write(downloadList.get(0).getContents());
            stream.flush();
            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("STREAMING_OUTPUT_FAILED");
            resultStatusInfo.setMessage("failed to stream file to frontend");
            return apiResponse;

        }

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("download/stream successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    public HirePlusPlusResponseBody downloadVideo(String interviewId, HttpServletResponse servletResponse) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<MemoryFile> downloadList;
        MemoryFile file;
        InterviewMediaFilesData interviewRepoData = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("Interview Id not found : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<String> fileList = new ArrayList<>();
        fileList.add(interviewId + "." + interviewRepoData.getVideoFileNameExtension());

        log.info(fileList.toString());
        String folderName = interviewId + "/video";

        try {
            downloadList = cloudStorageHandlerAzure.downloadFiles(folderName, fileList, connectionString, interviewContainer);
            if (downloadList == null || downloadList.isEmpty()) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No videoparts found in cloud for interviewId : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            log.info(downloadList.size() + "");

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("CLOUD_STORAGE_EXCEPTION");
            resultStatusInfo.setMessage("failed to get file from cloud");
            return apiResponse;

        }
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (MemoryFile file1 : downloadList) {
                outputStream.write(file1.getContents());
            }

//            servletResponse.setContentType("application/octet-stream");
            servletResponse.addHeader("Content-Disposition", "attachment; filename=" + interviewId + "." + interviewRepoData.getVideoFileNameExtension());
            byte[] result = outputStream.toByteArray();
            ServletOutputStream stream = servletResponse.getOutputStream();
            stream.write(result);
            stream.flush();
            stream.close();


        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("STREAMING_OUTPUT_FAILED");
            resultStatusInfo.setMessage("failed to stream file to frontend");
            return apiResponse;

        }

        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("download/stream successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    public HirePlusPlusResponseBody getAllSnapshotsAsBase64Images(String interviewId) {

        if (storageAccount.equalsIgnoreCase(AWS)) {
            return getAllSnapshotsAsBase64ImagesAws(interviewId);

        }


        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<String> apiDataList = new ArrayList<>();
        List<MemoryFile> downloadList;
        InterviewMediaFilesData interviewRepoData = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("Interview Id not found : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<String> fileList = new ArrayList<>();
        for (int i = 0; i < interviewRepoData.getSnapshotCount(); i++) {
            fileList.add(i + "." + interviewRepoData.getSnapshotFileNameExtension());
        }
        try {
            String folderName = interviewId + "/snapshots";
            downloadList = cloudStorageHandlerAzure.downloadFiles(folderName, fileList, connectionString, interviewContainer);
            if (downloadList == null || downloadList.isEmpty()) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No snapshots found in cloud for interviewId : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("CLOUD_STORAGE_EXCEPTION");
            resultStatusInfo.setMessage("failed to get file from cloud");
            return apiResponse;

        }

        for (MemoryFile file : downloadList) {
            String base64Image = Base64.getEncoder().encodeToString(file.getContents());
            base64Image = "data:image/" + interviewRepoData.getSnapshotFileNameExtension() + ";base64," + base64Image;
            apiDataList.add(base64Image);
        }
        log.info("total snapshotcount : " + apiDataList.size());
        apiResponse.setData(apiDataList);
        resultStatusInfo.setResultCode(ResultCode.Success.name());
        resultStatusInfo.setMessage("retrieval successful");
        apiResponse.setResultStatusInfo(resultStatusInfo);
        return apiResponse;
    }

    private HirePlusPlusResponseBody getAllSnapshotsAsBase64ImagesAws(String interviewId) {

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<byte[]> downloadedFilesList = new ArrayList<>();
        List<String> apiDataList = new ArrayList<>();
        InterviewMediaFilesData interviewRepoData = interviewRepo.getInterviewMediaFilesInfoById(interviewId);
        if (interviewRepoData == null) {
            resultStatusInfo.setResultCode(ResultCode.NotFound.name());
            resultStatusInfo.setMessage("Interview Id not found : " + interviewId);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

        List<String> fileList = new ArrayList<>();
        for (int i = 0; i < interviewRepoData.getSnapshotCount(); i++) {
            fileList.add(i + "." + interviewRepoData.getSnapshotFileNameExtension());
            //only 3 snapshots required in report
            if (i >= 2)
                break;
        }
        try {
            String folderName = interviewId + "/snapshots";
            apiResponse = cloudStorageHandlerAws.downloadFilesFromCloudStorage(folderName, fileList, interviewId);
            if (!apiResponse.getResultStatusInfo().getResultCode().equalsIgnoreCase("Success")) {
                resultStatusInfo.setResultCode(apiResponse.getResultStatusInfo().getResultCode());
                resultStatusInfo.setMessage("failed to download snapshots : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            downloadedFilesList = (List<byte[]>) apiResponse.getData();
            if (downloadedFilesList.size() == 0) {
                resultStatusInfo.setResultCode(ResultCode.NotFound.name());
                resultStatusInfo.setMessage("No snapshots found in cloud for interviewId : " + interviewId);
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            for (byte[] file : downloadedFilesList) {
                String base64Image = Base64.getEncoder().encodeToString(file);
                base64Image = "data:image/" + interviewRepoData.getSnapshotFileNameExtension() + ";base64," + base64Image;
                apiDataList.add(base64Image);
            }
            log.info("total snapshotCount : " + apiDataList.size());
            apiResponse.setData(apiDataList);
            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage("retrieval successful");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;


        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("CLOUD_STORAGE_EXCEPTION");
            resultStatusInfo.setMessage("failed to get file from cloud");
            return apiResponse;

        }


    }


    public void testaws(HttpServletResponse response) {
        cloudStorageHandlerAws.testDownload(response);
    }

    public void testUpload(MultipartFile file) {
        cloudStorageHandlerAws.testPresignedUrl();

    }


}
