package com.rabbitMq.rabbitmqscheduler.DTO.credential;

import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;

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