package com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferOptions;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
public class RequestFromODS {

    private String id;
    @NonNull
    private String accountId;
    @NonNull
    private String userId;
    private long chunkSize;
    @NonNull
    private Source source;
    @NonNull
    private Destination destination;
    private TransferOptions options;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Source {

        @NonNull
        private EndPointType type;
        @NonNull
        private EntityInfo parentInfo;
        @NonNull
        private ArrayList<EntityInfo> infoList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Destination {
        @NonNull
        private EndPointType type;
        @NonNull
        private EntityInfo parentInfo;
    }
}
