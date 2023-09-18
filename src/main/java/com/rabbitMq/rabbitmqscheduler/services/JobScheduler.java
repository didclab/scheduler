package com.rabbitMq.rabbitmqscheduler.services;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.rabbitMq.rabbitmqscheduler.model.RequestFromODSDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JobScheduler {

    IMap<UUID, RequestFromODSDTO> jobIMap;
    EntryExpiredHazelcast entryExpiredHazelcast;


    public JobScheduler(HazelcastInstance hazelcastInstance, RequestModifier requestModifier, MessageSender messageSender) {
        this.jobIMap = hazelcastInstance.getMap("scheduled_jobs");
        this.entryExpiredHazelcast = new EntryExpiredHazelcast(requestModifier, messageSender);
    }

    public Collection<RequestFromODSDTO> listScheduledJobs(String userEmail) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        return this.jobIMap.values(e.is("userEmail").and(e.get("userEmail").equal(userEmail)));
    }

    public RequestFromODSDTO getScheduledJobDetails(UUID jobUuid) {
        return this.jobIMap.get(jobUuid);
    }

    public void deleteScheduledJob(UUID jobUuid) {
        if (this.jobIMap.containsKey(jobUuid)) {
            this.jobIMap.delete(jobUuid);
        }
    }

    public boolean saveScheduledJob(RequestFromODSDTO transferRequest, Instant jobStartTime) {
        UUID id = UUID.randomUUID();
        long delay = Instant.now().until(jobStartTime, ChronoUnit.MILLIS);
        this.jobIMap.put(id, transferRequest, delay, TimeUnit.MILLISECONDS);
        this.jobIMap.addEntryListener(this.entryExpiredHazelcast, id, true);
        return true;
    }
}
