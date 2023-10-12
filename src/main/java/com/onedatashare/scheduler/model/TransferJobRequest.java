package com.onedatashare.scheduler.model;

import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.model.credential.OAuthEndpointCredential;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransferJobRequest {
    private String ownerId;
    private Source source;
    private Destination destination;
    private TransferOptions options;
    private String transferNodeName;
    private UUID jobUuid;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        private EndPointType type;
        String credId;
        private AccountEndpointCredential vfsDestCredential;
        private OAuthEndpointCredential oauthDestCredential;
        private String fileDestinationPath;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Source {
        private EndPointType type;
        String credId;
        private AccountEndpointCredential vfsSourceCredential;
        private OAuthEndpointCredential oauthSourceCredential;
        private String fileSourcePath;
        private List<EntityInfo> infoList;
    }
}