package com.onedatashare.scheduler.services;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapStore;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JobScheduler {

    protected IMap<UUID, RequestFromODS> jobIMap;
    private EntryExpiredHazelcast entryExpiredHazelcast;
    Logger logger = LoggerFactory.getLogger(JobScheduler.class);


    public JobScheduler(HazelcastInstance hazelcastInstance, RequestModifier requestModifier, MessageSender messageSender) {
        this.jobIMap = hazelcastInstance.getMap("scheduled_jobs");
        this.entryExpiredHazelcast = new EntryExpiredHazelcast(requestModifier, messageSender);
    }

    public Collection<RequestFromODS> listScheduledJobs(String userEmail) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        Predicate<UUID, RequestFromODS> predicate = entry -> entry.getValue().getOwnerId().equals(userEmail);
        return this.jobIMap.values(predicate);
    }

    public RequestFromODS getScheduledJobDetails(UUID jobUuid) {
        return this.jobIMap.get(jobUuid);
    }

    public boolean deleteScheduledJob(UUID jobUuid) {
        if (this.jobIMap.containsKey(jobUuid)) {
            this.jobIMap.delete(jobUuid);
            return true;
        }else{
            return false;
        }
    }

    public UUID saveScheduledJob(RequestFromODSDTO transferRequest, LocalDateTime jobStartTime) {
        if(transferRequest == null) {return null;}
        UUID id = UUID.randomUUID();
        Instant currentDate = Instant.now();
        RequestFromODS transferJob = new RequestFromODS();
        transferJob.setOptions(transferRequest.getOptions());
        transferJob.setDestination(transferRequest.getDestination());
        transferJob.setSource(transferRequest.getSource());
        transferJob.setOwnerId(transferRequest.getOwnerId());
        transferJob.setJobUuid(id);
        transferJob.setJobStartTime(jobStartTime);
        transferJob.setTransferNodeName(transferRequest.getTransferNodeName());

        long delay = Duration.between(LocalDateTime.now(), jobStartTime).getSeconds();
        logger.info("Now: {} \t jobStartTime: {} \t delay: {}", currentDate, jobStartTime, delay);
        if(delay < 0) delay = 1;
        this.jobIMap.put(id, transferJob, delay, TimeUnit.SECONDS);
        this.jobIMap.addEntryListener(this.entryExpiredHazelcast, id, true);
        return id;
    }
}
