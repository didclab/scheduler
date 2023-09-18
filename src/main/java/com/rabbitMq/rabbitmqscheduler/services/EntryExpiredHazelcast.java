package com.rabbitMq.rabbitmqscheduler.services;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.rabbitMq.rabbitmqscheduler.model.RequestFromODSDTO;
import com.rabbitMq.rabbitmqscheduler.model.TransferJobRequest;

import java.util.UUID;

public class EntryExpiredHazelcast implements EntryExpiredListener<UUID, RequestFromODSDTO> {

    private final RequestModifier requestModifier;
    private final MessageSender messageSender;

    public EntryExpiredHazelcast(RequestModifier requestModifier, MessageSender messageSender) {
        this.requestModifier = requestModifier;
        this.messageSender = messageSender;
    }

    @Override
    public void entryExpired(EntryEvent<UUID, RequestFromODSDTO> event) {
        TransferJobRequest transferJobRequest = requestModifier.createRequest(event.getValue());
        messageSender.sendTransferRequest(transferJobRequest);
    }
}
