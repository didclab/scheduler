package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileTransferNodeDiscovery {

    private final ObjectMapper objectMapper;
    private final IMap<String, HazelcastJsonValue> fileNodeMap;


    public FileTransferNodeDiscovery(IMap<String, HazelcastJsonValue> fileTransferNodeMap, ObjectMapper objectMapper) {
        this.fileNodeMap = fileTransferNodeMap;
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

    public List<FileTransferNodeMetaData> getOdsNodes(){
        PredicateBuilder.EntryObject e = new PredicateBuilderImpl();
        PredicateBuilder predicate = e.get("nodeName").equal("ODSTransferService").or(e.get("odsOwner").equal("OneDataShare"));
        Collection<HazelcastJsonValue> userFreeNodeJson = this.fileNodeMap.values(predicate);
        return userFreeNodeJson.stream().map(hazelcastJsonValue -> {
            try {
                return this.objectMapper.readValue(hazelcastJsonValue.getValue(), FileTransferNodeMetaData.class);
            } catch (JsonProcessingException ex) {return null;}
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

    public FileTransferNodeMetaData getFileTransferNode(String nodeName) throws JsonProcessingException {
        HazelcastJsonValue jsonValue = this.fileNodeMap.get(nodeName);
        return this.objectMapper.readValue(jsonValue.getValue(), FileTransferNodeMetaData.class);
    }

    public FileTransferNodeMetaData getNodeRunningJob(UUID jobUuid) throws JsonProcessingException {
        Predicate<String, HazelcastJsonValue> predicate = Predicates.equal("jobUuid", jobUuid);
        Collection<HazelcastJsonValue> nodes = this.fileNodeMap.values(predicate);
        for(HazelcastJsonValue node : nodes){
            FileTransferNodeMetaData fileTransferNodeMetaData = this.objectMapper.readValue(node.getValue(), FileTransferNodeMetaData.class);
            return fileTransferNodeMetaData;
        }
        return null;
    }

    public int totalConnectedFileTransferNodes() {
        return this.fileNodeMap.size();
    }




}
