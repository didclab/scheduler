package com.onedatashare.scheduler.services.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.listener.EntryAddedListener;
import com.onedatashare.scheduler.model.FileTransferNodeMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTransferNodeEventListener implements EntryAddedListener<String, HazelcastJsonValue> {

    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;
    private Logger logger;

    public FileTransferNodeEventListener(HazelcastInstance hazelcastInstance, ObjectMapper objectMapper){
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = objectMapper;
        this.logger = LoggerFactory.getLogger(FileTransferNodeEventListener.class);
    }

    @Override
    public void entryAdded(EntryEvent<String, HazelcastJsonValue> event) {
        String jsonEntryAdded = event.getValue().getValue();
        try {
            FileTransferNodeMetaData fileTransferNodeMetaData = this.objectMapper.readValue(jsonEntryAdded, FileTransferNodeMetaData.class);
            this.hazelcastInstance.getQueue(fileTransferNodeMetaData.getNodeName());
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse file transfer node metaData \n{}: \n{}", jsonEntryAdded, e.getMessage());
        }
    }
}
