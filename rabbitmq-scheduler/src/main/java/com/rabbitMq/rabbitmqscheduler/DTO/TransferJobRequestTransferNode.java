package com.rabbitMq.rabbitmqscheduler.DTO;

import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.HashSet;
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TransferJobRequestTransferNode {

    @NonNull
    protected com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest.Source source;
    @NonNull protected com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest.Destination destination;
    protected TransferOptions options;


    @Data
    @Accessors(chain = true)
    public static class Destination {
        @NonNull protected EndPointType type;
        @NonNull protected String credId;
        @NonNull protected com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest.EntityInfo info;
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Source {
        @NonNull protected EndPointType type;
        @NonNull protected String credId;
        @NonNull protected TransferJobRequest.EntityInfo info;
        @NonNull protected HashSet<TransferJobRequest.EntityInfo> infoList;


    }


}
