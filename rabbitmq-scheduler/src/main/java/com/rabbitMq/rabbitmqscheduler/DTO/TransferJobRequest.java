package com.rabbitMq.rabbitmqscheduler.DTO;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;

import lombok.*;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@Getter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = TransferJobRequest.class)
public class TransferJobRequest {


    private String jobId;
    private String ownerId;

    private int priority;

    private int chunkSize;
    private Source source;
    private Destination destination;
    private TransferOptions options;

    public TransferJobRequest(String jobId, String ownerId, int priority, int chunkSize) {
        this.jobId = jobId;
        this.ownerId = ownerId;
        this.priority = priority;
        this.chunkSize = chunkSize;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {


        private EndPointType type;
        private AccountEndpointCredential vfsDestCredential;
        private OAuthEndpointCredential oauthDestCredential;
        private EntityInfo parentInfo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Source {

        private EndPointType type;
        private AccountEndpointCredential vfsSourceCredentail;
        private OAuthEndpointCredential oauthSourceCredential;
        private EntityInfo parentInfo;
        private ArrayList<EntityInfo> infoList;
    }
}