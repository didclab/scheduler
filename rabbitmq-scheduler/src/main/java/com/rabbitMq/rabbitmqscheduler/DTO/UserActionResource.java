package com.rabbitMq.rabbitmqscheduler.DTO;


import java.util.ArrayList;

public class UserActionResource {
    private String uri;
    private String id;
    private UserActionCredential credential;
    private ArrayList<IdMap> map;
    private String type;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserActionCredential getCredential() {
        return credential;
    }

    public void setCredential(UserActionCredential credential) {
        this.credential = credential;
    }

    public ArrayList<IdMap> getMap() {
        return map;
    }

    public void setMap(ArrayList<IdMap> map) {
        this.map = map;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
