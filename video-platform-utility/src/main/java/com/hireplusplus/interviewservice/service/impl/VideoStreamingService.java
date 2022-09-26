/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.service.impl;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   16-08-2022
 ** @ClassName     :    VideoStreamingService
 ** @Summary       :
 ****************************************************************************************/
@Service
public class VideoStreamingService {

    @Autowired
    private ResourceLoader resourceLoader;

    public Mono<Resource> streamVideo(){
        return Mono.fromSupplier(()-> resourceLoader.getResource(String.format("1234")));
    }
}
