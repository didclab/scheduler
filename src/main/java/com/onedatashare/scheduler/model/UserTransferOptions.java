package com.onedatashare.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserTransferOptions implements Serializable {
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

    public UserTransferOptions(){
        this.compress = false;
        this.encrypt = false;
        this.optimizer = "";
        this.overwrite = false;
        this.retry = 0;
        this.verify = false;
        this.concurrencyThreadCount = 0;
        this.parallelThreadCount = 0;
        this.pipeSize = 0;
        this.chunkSize = 0;
    }

}
