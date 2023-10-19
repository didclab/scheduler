package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.model.credential.OAuthEndpointCredential;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class CredentialService {
    private String credListUrl;
    private static final Logger logger = LoggerFactory.getLogger(CredentialService.class);
    @Value("${cred.service.eureka.uri}")
    String credentialEureka;

    @Autowired
    RestTemplate restTemplate;

    @PostConstruct
    public void adjustUrl() {
        credListUrl = credentialEureka + "/{userId}/{type}/{accountId}";
    }

    public AccountEndpointCredential fetchAccountCredential(String type, String userId, String credId) {
        logger.info(type + ":" + userId + ":" + credId);
        return restTemplate.getForObject(credListUrl, AccountEndpointCredential.class, userId, type, credId);
    }

    public OAuthEndpointCredential fetchOAuthCredential(EndPointType type, String userId, String credId) {
        logger.info("The OAuth type is: " + type + "UserId is: " + userId + " CredId is:" + credId);
        return restTemplate.getForObject(credListUrl, OAuthEndpointCredential.class, userId, type, credId);
    }
}
