package com.onedatashare.scheduler.model;

import lombok.Data;

@Data
public class RequestFromODSDTO {

    private String ownerId;
    private FileSource source;
    private FileDestination destination;
    private UserTransferOptions options;
    private String transferNodeName;


    public RequestFromODS convertToPojo() {
        RequestFromODS request = new RequestFromODS();
        request.setDestination(this.destination);
        request.setSource(this.source);
        request.setTransferNodeName(this.transferNodeName);
        request.setOwnerId(this.ownerId);
        request.setOptions(this.options);
        return request;
    }
}
