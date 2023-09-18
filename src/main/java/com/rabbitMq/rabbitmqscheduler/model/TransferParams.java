package com.rabbitMq.rabbitmqscheduler.model;

import lombok.Data;

@Data
public class TransferParams {

    Integer concurrency;
    Integer parallelism;
    Integer pipelining;
    Long chunkSize;
    String transferNodeName;

}
