package com.rabbitMq.rabbitmqscheduler.DTO;

import lombok.Data;

@Data
public class StopRequest {
    Long jobId;
    String transferNodeName;

}
