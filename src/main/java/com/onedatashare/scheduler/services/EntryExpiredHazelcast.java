package com.onedatashare.scheduler.services;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.model.TransferJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EntryExpiredHazelcast implements EntryExpiredListener<UUID, RequestFromODS> {

    private final RequestModifier requestModifier;
    private final MessageSender messageSender;
    Logger logger = LoggerFactory.getLogger(EntryExpiredHazelcast.class);

    public EntryExpiredHazelcast(RequestModifier requestModifier, MessageSender messageSender) {
        this.requestModifier = requestModifier;
        this.messageSender = messageSender;
    }

    @Override
    public void entryExpired(EntryEvent<UUID, RequestFromODS> event) {
        TransferJobRequest transferJobRequest = requestModifier.createRequest(event.getOldValue());
        messageSender.sendTransferRequest(transferJobRequest);
    }
}
