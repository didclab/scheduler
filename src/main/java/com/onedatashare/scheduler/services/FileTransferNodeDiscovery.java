package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.IndexType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.onedatashare.scheduler.model.FileTransferNodeMetaData;
import com.onedatashare.scheduler.services.listeners.FileTransferNodeEventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileTransferNodeDiscovery {

    private final ObjectMapper objectMapper;
    private final IMap<String, HazelcastJsonValue> fileNodeMap;


    public FileTransferNodeDiscovery(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, ObjectMapper objectMapper) {
        this.fileNodeMap = hazelcastInstance.getMap("file-transfer-node-map");
        fileNodeMap.addIndex(IndexType.HASH, "odsOwner");
        fileNodeMap.addIndex(IndexType.HASH, "nodeName");
        fileNodeMap.addIndex(IndexType.HASH, "runningJob");
        fileNodeMap.addIndex(IndexType.HASH, "online");
        fileNodeMap.addEntryListener(new FileTransferNodeEventListener(hazelcastInstance, objectMapper), true);
        this.objectMapper = objectMapper;
    }

    public List<FileTransferNodeMetaData> getUsersFileTransferNodes(String odsUserName) {
        Predicate<String, HazelcastJsonValue> query = Predicates.equal("odsOwner", odsUserName);
        return this.fileNodeMap.values(query).stream().map(hazelcastJsonValue -> {
            try {
                return this.objectMapper.readValue(hazelcastJsonValue.getValue(), FileTransferNodeMetaData.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    public List<FileTransferNodeMetaData> getAvailableNodes(String odsUserName) {
        PredicateBuilder.EntryObject e = new PredicateBuilderImpl();
        Predicate<String, HazelcastJsonValue> getOwnerNodeAndFree = e.get("odsOwner").equal(odsUserName).or(e.get("odsOwner").equal("")).and(e.get("runningJob").equal("false"));
        Collection<HazelcastJsonValue> userFreeNodeJson = this.fileNodeMap.values(getOwnerNodeAndFree);
        return userFreeNodeJson.stream().map(hazelcastJsonValue -> {
            try {
                return this.objectMapper.readValue(hazelcastJsonValue.getValue(), FileTransferNodeMetaData.class);
            } catch (JsonProcessingException ex) {return null;}
        }).collect(Collectors.toList());
    }

    public int totalConnectedFileTransferNodes() {
        return this.fileNodeMap.size();
    }

}
