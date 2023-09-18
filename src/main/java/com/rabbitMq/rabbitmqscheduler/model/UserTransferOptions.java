package com.rabbitMq.rabbitmqscheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTransferOptions {
    private Boolean compress;
    private Boolean encrypt;
    private String optimizer;
    private Boolean overwrite;
    private Integer retry;
    private Boolean verify;
    private Integer concurrencyThreadCount;
    private Integer parallelThreadCount;
    private Integer pipeSize;
    private Integer chunkSize;

}
