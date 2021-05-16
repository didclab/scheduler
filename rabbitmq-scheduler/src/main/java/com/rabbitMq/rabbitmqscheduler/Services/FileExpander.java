package com.rabbitMq.rabbitmqscheduler.Services;

import com.ctc.wstx.ent.EntityDecl;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

public interface FileExpander {

    public void createClient(EndpointCredential credential);

    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources,String basePath);
}
