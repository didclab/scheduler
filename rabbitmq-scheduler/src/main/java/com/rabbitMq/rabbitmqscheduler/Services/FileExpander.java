package com.rabbitMq.rabbitmqscheduler.Services;

import com.ctc.wstx.ent.EntityDecl;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

public interface FileExpander {

    public void setCredential(EndpointCredential credential);

    public void createClient(List<EntityInfo> userSelectedResources);

    public List<EntityInfo> expandedFileSystem(String basePath);
}
