package com.rabbitMq.rabbitmqscheduler.DTO;

import lombok.Data;

@Data
public class UserTransferOptions {
    private Boolean compress;
    private Boolean encrypt;
    private String optimizer;
    private boolean overwrite;
    private Integer retry;
    private Boolean verify;
}
