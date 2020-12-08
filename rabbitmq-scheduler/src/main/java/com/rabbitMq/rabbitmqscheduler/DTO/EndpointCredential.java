package com.rabbitMq.rabbitmqscheduler.DTO;

public class EndpointCredential {
    protected String accountId;
    protected String passWord;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPassWord(){
        return passWord;
    }

    public void setPassWord(String passWord){
        this.passWord = passWord;
    }
}
