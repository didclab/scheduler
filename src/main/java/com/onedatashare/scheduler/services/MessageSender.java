package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.onedatashare.scheduler.enums.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    public MessageSender(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, ObjectMapper objectMapper) {
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Object obj, MessageType messageType, String transferNodeName) throws JsonProcessingException, InterruptedException {
        String jsonMsg = "{}";
        if (obj != null) {
            jsonMsg = this.objectMapper.writeValueAsString(obj);
        }
        JsonNode jsonNode = this.objectMapper.readTree(jsonMsg);
        jsonNode = ((ObjectNode) jsonNode).put("type", messageType.toString());

        IQueue<HazelcastJsonValue> iqueue = this.hazelcastInstance.getQueue(transferNodeName);
        iqueue.put(new HazelcastJsonValue(jsonNode.toString()));
        logger.info("Send Msg {} to node {}", jsonNode, transferNodeName);
    }

}