package com.onedatashare.scheduler.services.maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.query.LocalIndexStats;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.onedatashare.scheduler.model.TransferJobRequest;
import com.onedatashare.scheduler.services.MessageSender;
import com.onedatashare.scheduler.services.listeners.EntryExpiredHazelcast;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TransferSchedulerMapService {

    private final IMap<UUID, HazelcastJsonValue> jobScheduleMap;
    private final ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(TransferSchedulerMapService.class);

    public TransferSchedulerMapService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, ObjectMapper objectMapper, MessageSender messageSender) {
        this.jobScheduleMap = hazelcastInstance.getMap("file-transfer-schedule-map");
        this.objectMapper = objectMapper;
        this.jobScheduleMap.addEntryListener(new EntryExpiredHazelcast(messageSender), true);
    }

    @PostConstruct
    public void init() {
        Map<String, LocalIndexStats> indexs = this.jobScheduleMap.getLocalMapStats().getIndexStats();
        if(!indexs.containsKey("ownerId")){
            this.jobScheduleMap.addIndex(IndexType.HASH, "ownerId");
        }
        if(!indexs.containsKey("jobUuid")) {
            this.jobScheduleMap.addIndex(IndexType.HASH, "jobUuid");
        }
    }

    public Collection<UUID> getKeys() {
        return this.jobScheduleMap.keySet();
    }
    public Collection<UUID> getLocalKeys() { return this.jobScheduleMap.localKeySet();}

    public TransferJobRequest getValue(UUID uuid) throws JsonProcessingException {
        HazelcastJsonValue hazelcastJsonValue = this.jobScheduleMap.get(uuid);
        return this.objectMapper.readValue(hazelcastJsonValue.getValue(), TransferJobRequest.class);
    }

    public List<TransferJobRequest> queryUserScheduledJobs(String userEmail) throws JsonProcessingException {
        Collection<HazelcastJsonValue> values = this.jobScheduleMap.values(Predicates.equal("ownerId", userEmail));
        List<TransferJobRequest> scheduledJobsForUser = new ArrayList<>();
        for (HazelcastJsonValue value : values) {
            TransferJobRequest transferJobRequest = this.objectMapper.readValue(value.getValue(), TransferJobRequest.class);
            scheduledJobsForUser.add(transferJobRequest);
        }
        logger.info("Queried Jobs: {}", scheduledJobsForUser);
        return scheduledJobsForUser;
    }

    public TransferJobRequest queryJob(UUID jobUuid) throws JsonProcessingException {
        HazelcastJsonValue hazelcastJsonValue = this.jobScheduleMap.get(jobUuid);
        return this.objectMapper.readValue(hazelcastJsonValue.getValue(), TransferJobRequest.class);
    }

    public void deleteJob(UUID jobUuid) {
        this.jobScheduleMap.delete(jobUuid);
    }

    public void putJob(TransferJobRequest transferJobRequest, LocalDateTime jobStartTime) throws JsonProcessingException {
        long delay = Duration.between(LocalDateTime.now(), jobStartTime).getSeconds();
        if(delay < 0) {
            delay = 1;
        }
        String jsonRequestValue = this.objectMapper.writeValueAsString(transferJobRequest);
        logger.info("Putting File Transfer Job in Map: \n {}", jsonRequestValue);
        this.jobScheduleMap.put(transferJobRequest.getJobUuid(), new HazelcastJsonValue(jsonRequestValue), delay, TimeUnit.SECONDS);
    }

    public boolean containsKey(UUID jobUuid) {
        return this.jobScheduleMap.containsKey(jobUuid);
    }
}
