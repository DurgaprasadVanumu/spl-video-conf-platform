/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.config;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/


import com.amazonaws.auth.AWSCredentials;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   17-08-2022
 ** @ClassName     :    AwsStorageConfiguration
 ** @Summary       :
 ****************************************************************************************/
@Configuration
public class AwsStorageConfiguration {

    @Value("${aws.s3.accessKey}")
    private String accessKey;
    @Value("${aws.s3.secretKey}")
    private String secretKey;
    @Value("${aws.s3.region}")
    private String region;
    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Bean
    public AmazonS3 generateS3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }

    public String getBucketName(){
        return bucketName;
    }
}
