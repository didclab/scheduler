package com.rabbitMq.rabbitmqscheduler.DTO;

public class EndpointCredential {
    protected String accountId;
    protected String password;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    @Override
    public String toString() {
        return "EndpointCredential{" +
                "accountId='" + accountId + '\'' +
                ", passWord='" + password + '\'' +
                '}';
    }
}
