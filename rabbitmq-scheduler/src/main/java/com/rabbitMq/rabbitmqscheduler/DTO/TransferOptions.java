package com.rabbitMq.rabbitmqscheduler.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class TransferOptions {
    private Boolean compress;
    private Boolean encrypt;
    private String optimizer;
    private boolean overwrite;
    private Integer retry;
    private Boolean verify;
    private int concurrencyThreadCount;
    private int parallelThreadCount;
    private int pipeSize;

    public static TransferOptions createTransferOptionsFromUser(UserTransferOptions userTransferOptions) {
        return TransferOptions.builder()
                .compress(userTransferOptions.getCompress())
                .concurrencyThreadCount(1)
                .pipeSize(1)
                .parallelThreadCount(0)
                .optimizer(userTransferOptions.getOptimizer())
                .encrypt(userTransferOptions.getEncrypt())
                .overwrite(userTransferOptions.isOverwrite())
                .retry(userTransferOptions.getRetry())
                .verify(userTransferOptions.getVerify())
                .build();
    }

}
