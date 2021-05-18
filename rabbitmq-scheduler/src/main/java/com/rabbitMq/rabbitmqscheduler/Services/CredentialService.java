package com.rabbitMq.rabbitmqscheduler.Services;

import com.netflix.discovery.EurekaClient;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
public class CredentialService {
    private static final Logger logger = LoggerFactory.getLogger(CredentialService.class);
    private String credListUrl;

    @Value("${cred.service.eureka.uri}")
    String credentialEureka;


    @Autowired
    RestTemplate eurekaTemplate;


    @PostConstruct
    public void adjustUrl(){

        credListUrl = credentialEureka+"/{userId}/{type}/{accountId}";
    }

    public AccountEndpointCredential fetchAccountCredential(EndPointType type, String userId, String credId){
        AccountEndpointCredential credential =eurekaTemplate.getForObject(credListUrl, AccountEndpointCredential.class, userId, type, credId);
        logger.info(credential.toString());
        return credential;
    }

    public OAuthEndpointCredential fetchOAuthCredential(EndPointType type, String userId, String credId){
        OAuthEndpointCredential credential =eurekaTemplate.getForObject(credListUrl, OAuthEndpointCredential.class, userId, type, credId);
        logger.info(credential.toString());
        return credential
    }
}
