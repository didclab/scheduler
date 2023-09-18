package com.rabbitMq.rabbitmqscheduler.model.credential;

import lombok.Data;

/**
 * Base class for storing one user credential
 */
@Data
public class EndpointCredential {
    protected String accountId;

    public EndpointCredential(){}
    public EndpointCredential(String accountId){
        this.accountId = accountId;
    }

    public static AccountEndpointCredential getAccountCredential(EndpointCredential endpointCredential){
        if(endpointCredential instanceof  AccountEndpointCredential){
            return (AccountEndpointCredential) endpointCredential;
        }else{
            return null;
        }
    }
    public static OAuthEndpointCredential getOAuthCredential(EndpointCredential endpointCredential){
        if(endpointCredential instanceof OAuthEndpointCredential){
            return (OAuthEndpointCredential) endpointCredential;
        }else{
            return null;
        }
    }
}