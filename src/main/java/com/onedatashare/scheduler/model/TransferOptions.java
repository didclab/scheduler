package com.onedatashare.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
                .compress(userTransferOptions.getCompress() != null && userTransferOptions.getCompress())
                .concurrencyThreadCount(userTransferOptions.getConcurrencyThreadCount() == null? 1: userTransferOptions.getConcurrencyThreadCount())
                .pipeSize(userTransferOptions.getPipeSize() == null ? 1: userTransferOptions.getPipeSize())
                .parallelThreadCount(userTransferOptions.getParallelThreadCount() == null? 0 : userTransferOptions.getParallelThreadCount() )
                .optimizer(userTransferOptions.getOptimizer() == null ? "" : userTransferOptions.getOptimizer())
                .encrypt(userTransferOptions.getEncrypt() != null && userTransferOptions.getEncrypt())
                .overwrite(userTransferOptions.getOverwrite())
                .retry(userTransferOptions.getRetry() == null ? 1: userTransferOptions.getRetry())
                .verify(userTransferOptions.getVerify() != null && userTransferOptions.getVerify())
                .build();
    }

}
