/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.service.impl;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/


import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.hireplusplus.interviewservice.enums.ResultCode;
import com.hireplusplus.interviewservice.models.api.HirePlusPlusResponseBody;
import com.hireplusplus.interviewservice.models.api.ResultStatusInfo;
import com.hireplusplus.interviewservice.models.db.InterviewMediaFilesData;
import com.hireplusplus.interviewservice.repository.InterviewMediaFilesRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   17-08-2022
 ** @ClassName     :    CloudStorageHandlerAws
 ** @Summary       :
 ****************************************************************************************/
@Service
public class CloudStorageHandlerAws {

    @Autowired
    private AmazonS3 s3Client;
    @Value("${aws.s3.bucketName}")
    private String bucketName;
    @Autowired
    private InterviewMediaFilesRepository interviewRepo;

    public HirePlusPlusResponseBody uploadFileToCloudStorage(InputStream inputStream, String folderName, String fileName, String interviewId) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        try {

            String fileExtension = FilenameUtils.getExtension(fileName);
            String objectKey = folderName + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            if (fileExtension.equalsIgnoreCase("jpg")
                    || fileExtension.equalsIgnoreCase("jpeg")
                    || fileExtension.equalsIgnoreCase("png")) {

                metadata.setContentType("image/" + fileExtension);

            } else if (fileExtension.equalsIgnoreCase("mp4")
                    || fileExtension.equalsIgnoreCase("webm")
                    || fileExtension.equalsIgnoreCase("mkv")) {
                metadata.setContentType("video/" + fileExtension);


            }else if(fileExtension.equalsIgnoreCase("pdf")){
                metadata.setContentType("application/pdf");

            }else if(fileExtension.equalsIgnoreCase("docx")
            ||fileExtension.equalsIgnoreCase("doc")){
                metadata.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            }
            metadata.setContentLength(inputStream.available());

            PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
            s3Client.putObject(request);

            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage("file upload to cloud storage successful");
            apiResponse.setResultStatusInfo(resultStatusInfo);

            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("AWS_UPLOAD_EXCEPTION");
            resultStatusInfo.setMessage("file upload to aws cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }

    }

    public HirePlusPlusResponseBody downloadFilesFromCloudStorage(String folderName, List<String> fileList, String interviewId) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<byte[]> downloadedFilesList = new ArrayList<>();
        try {
            for (String fileName : fileList) {
                String objectKey = folderName + "/" + fileName;
                S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
                InputStream i = object.getObjectContent();
                downloadedFilesList.add(IOUtils.toByteArray(i));
            }

            apiResponse.setData(downloadedFilesList);
            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage("file upload to cloud storage successful");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("AWS_S3_DOWNLOAD_EXCEPTION");
            resultStatusInfo.setMessage("download from cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }

    }

    public HirePlusPlusResponseBody deleteFilesFromCloudStorage(String folderName, List<String> fileList, String interviewId) {

        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();
        List<DeleteObjectsRequest.KeyVersion> deleteList = new ArrayList<>();
        try {
            for (String fileName : fileList) {
                String objectKey = folderName + "/" + fileName;
                deleteList.add(new DeleteObjectsRequest.KeyVersion(objectKey));
            }

            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(deleteList);
            DeleteObjectsResult response = s3Client.deleteObjects(deleteObjectsRequest);
            if (response.getDeletedObjects().size() == deleteList.size()) {

                resultStatusInfo.setResultCode(ResultCode.Success.name());
                resultStatusInfo.setMessage("files deleted from cloud successfully");
                apiResponse.setResultStatusInfo(resultStatusInfo);
                return apiResponse;
            }
            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage(response.getDeletedObjects().size() + " files out of " + deleteList.size() + " deleted from cloud");
            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("AWS_S3_DELETE_EXCEPTION");
            resultStatusInfo.setMessage("delete from cloud storage failed");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;

        }

    }

    public HirePlusPlusResponseBody getPreSignedUrl(String objectKey, int hours) {
        HirePlusPlusResponseBody apiResponse = new HirePlusPlusResponseBody();
        ResultStatusInfo resultStatusInfo = new ResultStatusInfo();

        try {

            long expTimeMillis = Instant.now().toEpochMilli();
            expTimeMillis += hours * 60 * 60 * 1000;
            Date expiryTime = new Date();
            expiryTime.setTime(expTimeMillis);

            // Generate the presigned URL.
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiryTime);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            System.out.println("Pre-Signed URL: " + url.toString());

            apiResponse.setData(url.toString());
            resultStatusInfo.setResultCode(ResultCode.Success.name());
            resultStatusInfo.setMessage("presigned url generated successfully");
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }  catch (Exception e) {
            e.printStackTrace();
            resultStatusInfo.setResultCode("AWS_S3_PRE_SIGNED_EXCEPTION");
            resultStatusInfo.setMessage("failed to generate presigned url for object : "+objectKey);
            apiResponse.setResultStatusInfo(resultStatusInfo);
            return apiResponse;
        }
    }


    public void testDelete() {

        List<DeleteObjectsRequest.KeyVersion> deleteList = new ArrayList<>();
        deleteList.add(new DeleteObjectsRequest.KeyVersion("video"));
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(deleteList);

        DeleteObjectsResult resp = s3Client.deleteObjects(deleteObjectsRequest);
        System.out.println(resp.getDeletedObjects().size());
    }

    public void testDownload(HttpServletResponse response) {
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, "video/10.mp4"));
        System.out.println(object.getKey());
        System.out.println(object.getObjectContent().toString());
        try {
            InputStream i = object.getObjectContent();

//            OutputStream outputStream = new ByteArrayOutputStream();
//            outputStream.write(IOUtils.toByteArray(i));
            ServletOutputStream stream = response.getOutputStream();
            stream.write(IOUtils.toByteArray(i));
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testUpload(MultipartFile file) {
        try {
            byte[] b = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(b);
            inputStream.close();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("video/mp4");
            PutObjectRequest request = new PutObjectRequest(bucketName, "video/101.mp4", inputStream, metadata);

            request.setMetadata(metadata);
            s3Client.putObject(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    int count = 0;
    List<PartETag> partETags = new ArrayList<PartETag>();
    InitiateMultipartUploadRequest initRequest = null;
    InitiateMultipartUploadResult initResponse = null;
    long fileoffset = 0;

    public void testMultipartAndConcat(MultipartFile file) {

        // Initiate the multipart upload.

        try {
            File file2 = new File("filename.mp4");
            FileOutputStream out = new FileOutputStream(file2);
            out.write(file.getBytes());
            out.close();
            if (count == 0) {
                initRequest = new InitiateMultipartUploadRequest(bucketName, "video/final.mp4");
                initResponse = s3Client.initiateMultipartUpload(initRequest);
                System.out.println(initResponse.getUploadId());
                System.out.println(initResponse.getKey());
            }
            byte[] b = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(b);
            inputStream.close();
            System.out.println(inputStream.available());
            if (count == 1) {

                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey("video/final.mp4")
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(1)
                        .withFileOffset(fileoffset)
                        .withFile(file2)
                        .withPartSize(b.length);
                fileoffset = b.length;
                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());
                count++;
            }


            if (count == 2) {
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey("video/final.mp4")
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(2)
                        .withFileOffset(fileoffset)
                        .withFile(file2)
                        .withPartSize(b.length);
                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());
                count++;
            }
            if (count == 3) {
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, "video/final.mp4",
                        initResponse.getUploadId(), partETags);
                s3Client.completeMultipartUpload(compRequest);
            }
            System.out.println(count);
            count++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testListOfMultipartUploads() {
        ListMultipartUploadsRequest allMultpartUploadsRequest =
                new ListMultipartUploadsRequest(bucketName);
        MultipartUploadListing multipartUploadListing =
                s3Client.listMultipartUploads(allMultpartUploadsRequest);
        multipartUploadListing.getMultipartUploads().forEach((part) -> {
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
                    bucketName, "video/final.mp4", part.getUploadId()));


        });
        System.out.println(multipartUploadListing.getMultipartUploads());

    }

    public void testPresignedUrl() {
        try {

            Date expiration = new Date();
            long expTimeMillis = Instant.now().toEpochMilli();
            expTimeMillis += 1000 * 60 * 60;
            expiration.setTime(expTimeMillis);

            // Generate the presigned URL.
            System.out.println("Generating pre-signed URL.");
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, "1234/video/1234.webm")
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            System.out.println("Pre-Signed URL: " + url.toString());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }

}
