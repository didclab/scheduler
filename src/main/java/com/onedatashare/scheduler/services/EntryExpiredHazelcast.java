package com.onedatashare.scheduler.services;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.TransferJobRequest;

import java.util.UUID;

public class EntryExpiredHazelcast implements EntryExpiredListener<UUID, RequestFromODS> {

    private final RequestModifier requestModifier;
    private final MessageSender messageSender;

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
