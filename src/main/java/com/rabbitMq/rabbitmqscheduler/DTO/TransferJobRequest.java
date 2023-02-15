package com.rabbitMq.rabbitmqscheduler.DTO;

import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;


import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = TransferJobRequest.class)
public class TransferJobRequest {
    private String ownerId;
    private int priority;
    private int chunkSize;
    private Source source;
    private Destination destination;
    private TransferOptions options;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        private EndPointType type;
        String credId;
        private AccountEndpointCredential vfsDestCredential;
        private OAuthEndpointCredential oauthDestCredential;
        private EntityInfo parentInfo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Source {
        private EndPointType type;
        String credId;
        private AccountEndpointCredential vfsSourceCredential;
        private OAuthEndpointCredential oauthSourceCredential;
        private EntityInfo parentInfo;
        private List<EntityInfo> infoList;
    }
}