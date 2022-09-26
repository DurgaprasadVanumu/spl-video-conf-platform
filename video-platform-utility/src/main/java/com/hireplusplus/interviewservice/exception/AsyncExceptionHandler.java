/****************************************************************************************
 **  							Blitzigo
 ****************************************************************************************/
package com.hireplusplus.interviewservice.exception;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   02-05-2022
 ** @Summary    :
 ****************************************************************************************/
@Slf4j
@ToString
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Method name : {}", method);
        log.error("Exception message : {}", ex.getMessage());
        log.error("params : {}", params);
    }
}
