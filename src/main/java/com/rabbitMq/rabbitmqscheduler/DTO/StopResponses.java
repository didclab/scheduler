package com.rabbitMq.rabbitmqscheduler.DTO;

public class StopResponses {
    private boolean success;
    private String errorMessage;

    public StopResponses(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
