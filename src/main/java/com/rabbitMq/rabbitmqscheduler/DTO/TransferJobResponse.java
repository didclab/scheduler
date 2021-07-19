package com.rabbitMq.rabbitmqscheduler.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class TransferJobResponse {
    String id;
    String message;
    String error;
}
