package com.rabbitMq.rabbitmqscheduler.services;

import com.rabbitMq.rabbitmqscheduler.model.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.model.credential.EndpointCredential;

import java.util.List;

public interface FileExpander {

    /**
     * This is meant to take our Credential abstraction and then translate that into the client for a specfici endpoint type.
     * @param credential
     */
    public void createClient(EndpointCredential credential);

    /**
     *This is the actual method that goes through all user selected folders/files and aggregates the information for the transfer-service
     *
     * @param userSelectedResources these are resources that the user gives to us to move. They all must be contained within the basePath
     * @param basePath: This is the path to the file we want to expand. This can also be an id for an OAuth flat file system basically
     * @return
     */
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources,String basePath);

}
