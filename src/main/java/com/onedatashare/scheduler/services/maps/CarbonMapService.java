package com.onedatashare.scheduler.services.maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.onedatashare.scheduler.model.carbon.CarbonMeasurement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class CarbonMapService {

    private final ObjectMapper objectMapper;
    private final IMap<UUID, HazelcastJsonValue> carbonIntensityMap;
    private Logger logger = LoggerFactory.getLogger(CarbonMapService.class);

    private final String ownerIdKey = "ownerId";
    private final String transferNodeNameKey = "transferNodeName";
    private final String jobUuidKey = "jobUuid";

    public CarbonMapService(IMap<UUID, HazelcastJsonValue> carbonIntensityMap, ObjectMapper objectMapper) {
        this.carbonIntensityMap = carbonIntensityMap;
        this.objectMapper = objectMapper;
    }


    public List<CarbonMeasurement> getCarbonMeasurementsForUserAndJob(String ownerId, UUID jobUuid) throws JsonProcessingException {
        return this.queryCarbonMeasurementValues(Predicates.and(Predicates.equal(this.ownerIdKey, ownerId), Predicates.equal(this.jobUuidKey, jobUuid.toString())));
    }

    public CarbonMeasurement getLatestCarbonEntryForJob(UUID uuid) throws JsonProcessingException {
        List<CarbonMeasurement> values = this.queryCarbonMeasurementValues(Predicates.equal(this.jobUuidKey, uuid.toString()));
        CarbonMeasurement latestKeyForJob = null;
        for (CarbonMeasurement value : values) {
            if (latestKeyForJob == null) {
                latestKeyForJob = value;
                continue;
            }
            if (value.getTimeMeasuredAt().isBefore(latestKeyForJob.getTimeMeasuredAt())) {
                latestKeyForJob = value;
            }
        }
        return latestKeyForJob;
    }

    public List<CarbonMeasurement> getAllCarbonIntensityForJob(UUID uuid) throws JsonProcessingException {
        logger.info("Querying Carbon Measurements for Job with UUID: {}", uuid);
        return this.queryCarbonMeasurementValues(Predicates.equal(this.jobUuidKey, uuid.toString()));
    }

    public List<CarbonMeasurement> getUserAndTransferNodeCarbonEntriesForJob(UUID jobUuid, String transferNodeName, String userEmail) throws JsonProcessingException {
        logger.info("Querying Carbon Entries for Job: jobUuid={}, transferNodeName={}, userEmail={}", jobUuid, transferNodeName, userEmail);
        return this.queryCarbonMeasurementValues(Predicates.and(Predicates.equal(this.jobUuidKey, jobUuid.toString()), Predicates.equal(this.transferNodeNameKey, transferNodeName), Predicates.equal(this.ownerIdKey, userEmail)));
    }

    public List<CarbonMeasurement> getAllUserEntries(String userEmail) throws JsonProcessingException {
        ;
        return queryCarbonMeasurementValues(Predicates.and(Predicates.equal(this.ownerIdKey, userEmail)));
    }

    public List<CarbonMeasurement> getNodeCarbonMeasurements(String transferNodeName) throws JsonProcessingException {
        return queryCarbonMeasurementValues(Predicates.equal(this.transferNodeNameKey, transferNodeName));
    }

    @NotNull
    private List<CarbonMeasurement> queryCarbonMeasurementValues(Predicate<UUID, HazelcastJsonValue> predicate) throws JsonProcessingException {
        logger.info("Carbon Map size: {}", this.carbonIntensityMap.size());
        Collection<HazelcastJsonValue> values = this.carbonIntensityMap.values(predicate);
        List<CarbonMeasurement> retList = new ArrayList<>();
        for (HazelcastJsonValue value : values) {
            CarbonMeasurement measurement = this.objectMapper.readValue(value.toString(), CarbonMeasurement.class);
            retList.add(measurement);
        }

        return retList;
    }
}
