package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpExpander extends DestinationChunkSize implements FileExpander{
    @Override
    public void createClient(EndpointCredential credential) {

    }

    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        return null;
    }
}
