package com.onedatashare.scheduler.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestFromODS implements Serializable {

    LocalDateTime jobStartTime;
    UUID jobUuid;
    String ownerId;
    FileSource source;
    FileDestination destination;
    UserTransferOptions options;
    String transferNodeName;
}
