package com.rabbitMq.rabbitmqscheduler.DTO;


public class TransferRequest {
    UserActionResource src;
    UserActionResource dest;
    TransferOptions options;

    public UserActionResource getSrc() {
        return src;
    }

    public void setSrc(UserActionResource src) {
        this.src = src;
    }

    public UserActionResource getDest() {
        return dest;
    }

    public void setDest(UserActionResource dest) {
        this.dest = dest;
    }

    public TransferOptions getOptions() {
        return options;
    }

    public void setOptions(TransferOptions options) {
        this.options = options;
    }
}
