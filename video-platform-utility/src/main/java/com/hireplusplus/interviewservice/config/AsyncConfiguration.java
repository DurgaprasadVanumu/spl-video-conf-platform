/****************************************************************************************
 **  							Blitzigo
 ****************************************************************************************/
package com.hireplusplus.interviewservice.config;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import com.hireplusplus.interviewservice.exception.AsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   02-05-2022
 ** @Summary    :
 ****************************************************************************************/
@Configuration
public class AsyncConfiguration extends AsyncConfigurerSupport{

    @Bean("AzureUploadAsyncExecutor")
    public Executor getEmailAsyncExecutor(){

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(4);
        executor.setThreadNamePrefix("Azure upload Async --");
        executor.initialize();
        return executor;

    }

    @Bean("AWSUploadAsyncExecutor")
    public Executor getSMSAsyncExecutor(){

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(4);
        executor.setThreadNamePrefix("AWS Async --");
        executor.initialize();
        return executor;

    }

    @Autowired
    public AsyncExceptionHandler asyncExceptionHandler;
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return asyncExceptionHandler;
    }
}
