package com.rabbitMq.rabbitmqscheduler.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
public class RequestFromODSDTO {

    private String ownerId;
    private FileSource source;
    private FileDestination destination;
    private UserTransferOptions options;
    private String transferNodeName;

}
