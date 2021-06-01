package com.rabbitMq.rabbitmqscheduler.DTO;

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
    private boolean overwrite;
    private Integer retry;
    private Boolean verify;
    private Integer concurrencyThreadCount;
    private Integer parallelThreadCount;
    private Integer pipeSize;
    private Integer chunkSize;

}
