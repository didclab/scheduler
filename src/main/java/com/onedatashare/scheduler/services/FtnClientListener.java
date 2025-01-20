package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.config.IndexType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.query.LocalIndexStats;
import com.onedatashare.scheduler.model.FileTransferNodeMetaData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FtnClientListener implements ClientListener {

    private final ObjectMapper objectMapper;
    private final HazelcastInstance hazelcastInstance;

    IMap<String, HazelcastJsonValue> fileTransferNodeMap;
    Logger logger = LoggerFactory.getLogger(FtnClientListener.class);

    public FtnClientListener(ObjectMapper objectMapper, @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, IMap<String, HazelcastJsonValue> fileTransferNodeMap) {
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = objectMapper;
        this.fileTransferNodeMap = fileTransferNodeMap;
    }

    @PostConstruct
    public void init() {
        Map<String, LocalIndexStats> indexStats = this.fileTransferNodeMap.getLocalMapStats().getIndexStats();
        if(!indexStats.containsKey("odsOwner")) this.fileTransferNodeMap.addIndex(IndexType.HASH, "odsOwner");
        if(!indexStats.containsKey("nodeName")) this.fileTransferNodeMap.addIndex(IndexType.HASH, "nodeName");
        this.hazelcastInstance.getClientService().addClientListener(this);
    }

    @Override
    public void clientConnected(Client client) {
        logger.debug("Client connected: {}", client.toString());
    }

    @Override
    public void clientDisconnected(Client client) {
        logger.debug("Client disconnected: {}", client.toString());
        List<String> labels = client.getLabels().stream().toList();
        try {
            this.setClientToOffline(labels.getFirst());
        } catch (JsonProcessingException e) {
            logger.error("Failed to set client to offline: {}", client.toString());
        }
    }

    public void setClientToOffline(String ftnAppName) throws JsonProcessingException {
        if (this.fileTransferNodeMap == null) {
            return;
        }
        HazelcastJsonValue jsonValue = this.fileTransferNodeMap.get(ftnAppName);
        FileTransferNodeMetaData fileTransferNodeMetaData = this.objectMapper.readValue(jsonValue.getValue(), FileTransferNodeMetaData.class);
        fileTransferNodeMetaData.setOnline(false);
        HazelcastJsonValue hazelcastJsonValue = new HazelcastJsonValue(this.objectMapper.writeValueAsString(fileTransferNodeMetaData));
        this.fileTransferNodeMap.put(ftnAppName, hazelcastJsonValue);
    }

}
