/****************************************************************************************
 **  							HirePlusPlus
 ****************************************************************************************/
package com.hireplusplus.interviewservice.config;
/****************************************************************************************
 **								Imports
 ****************************************************************************************/

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/****************************************************************************************
 ** @Author        :   SRAVAN AKHIL
 ** @Created-on    :   18-04-2022
 ** @Summary    :
 ****************************************************************************************/
@Configuration
public class OpenApi3Configuration {
    @Value("${application.name}")
    public String appName;

    @Value("${application.description}")
    public String appDescription;

    @Value("${application.documentation.wiki}")
    public String appDocumentationWiki;

    @Bean
    public OpenAPI customAPIConfiguration(){
        return new OpenAPI()
                .info(new Info().title(appName)
                        .description(appDescription)
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("HirePlusPlus Scheduler Service Documentation wiki")
                        .url(appDocumentationWiki));
    }
}
