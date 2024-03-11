package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onedatashare.scheduler.enums.MessageType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;

@Service
public class ODSRouter {

    private final ZMQ.Socket routerSocket;
    private final Thread routerThread;

    private final ObjectMapper objectMapper;
    //username -> {transferNodeName: Bool}
    Map<String, Map<String, Boolean>> userNodeMapping; //Username -> <TransferNodeName, True>
    Logger logger = LoggerFactory.getLogger(ODSRouter.class);

    public ODSRouter(ZContext zContext) {
        this.userNodeMapping = new HashMap<>();
        this.routerSocket = zContext.createSocket(SocketType.ROUTER);
        this.routerSocket.bind("tcp://localhost:5671");
        this.routerThread = new Thread(this::checkRouterSocket);
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Map<String, Boolean>> getUserNodeMap(){
        return this.userNodeMapping;
    }

    @PostConstruct
    public void start() {
        this.routerThread.start();
        logger.info("Router Thread: is Alive: {}", this.routerThread.isAlive());
    }

    @PreDestroy
    public void destroy() {
        this.routerSocket.close();
    }

    public void checkRouterSocket() {
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg msg = ZMsg.recvMsg(this.routerSocket);
            String identity = msg.unwrap().toString();
            String message = msg.getLast().toString();
            if (message.equals("REGISTER")) {
                this.newTransferNodeRegistered(identity);
            } else if (message.equals("DEREGISTER")) {
                this.removeTransferNode(identity);
            }
        }
    }

    public void newTransferNodeRegistered(String identity) {
        String[] ids = identity.split("-"); //0=userName, 1=transferNodeName, 2=uuid of node
        String userName = ids[0];
        String nodeNameAndUUID = ids[1] + "-" + ids[2];
        Map<String, Boolean> userNodes = this.userNodeMapping.get(userName);
        if (userNodes == null) {
            HashMap<String, Boolean> nodeMap = new HashMap<>();
            nodeMap.put(nodeNameAndUUID, true);
            this.userNodeMapping.put(userName, nodeMap);
        } else {
            userNodes.putIfAbsent(nodeNameAndUUID, true);
        }
        logger.info("New FTN {} registered. Size of Map: {}", identity, this.userNodeMapping.size());
    }

    public void removeTransferNode(String identity) {
        String[] ids = identity.split("-");
        String userName = ids[0];
        String nodeNameAndUUID = ids[1] + "-" + ids[2];
        Map<String, Boolean> userNodes = this.userNodeMapping.get(userName);
        if (userNodes != null) {
            Boolean transferNodeRegistered = userNodes.get(nodeNameAndUUID);
            if (transferNodeRegistered != null) {
                userNodes.remove(nodeNameAndUUID);
            }
        }
        logger.info("FTN de-registered: {}, Size of Map {}", identity, this.userNodeMapping.size());
    }

    /**
     * Identity is in the format of "USERNAME-APPNAME-UUID"
     *
     * @param object
     * @param identity
     * @return
     */
    public boolean sendPojo(Object object, String identity, MessageType messageType) {
        ObjectNode objectNode = this.objectMapper.valueToTree(object);
        objectNode.put("type", messageType.toString());
        String json = objectNode.toString();
        logger.info("Sending Message to {} message is: {}", identity, json);
        this.routerSocket.sendMore(identity.getBytes());
        this.routerSocket.sendMore("");
        return routerSocket.send(json, 0);
    }
}
