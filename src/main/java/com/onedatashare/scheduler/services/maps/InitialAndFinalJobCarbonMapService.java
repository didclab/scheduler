package com.onedatashare.scheduler.services.maps;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.onedatashare.scheduler.model.InitialAndFinal;
import com.onedatashare.scheduler.model.carbon.CarbonMeasurement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InitialAndFinalJobCarbonMapService {

    //jobUUid to initial Lowest Carbon Measurement
    private final IMap<UUID, CarbonMeasurement> initialMeasurementMap;
    private final IMap<UUID, CarbonMeasurement> finalMeasurementMap;

    public InitialAndFinalJobCarbonMapService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        this.initialMeasurementMap = hazelcastInstance.getMap("initial-transfer-measurements");
        this.finalMeasurementMap = hazelcastInstance.getMap("final-transfer-measurements");
    }

    public void putStartMeasurementForJob(UUID jobUuid, CarbonMeasurement carbonMeasurement) {
        this.initialMeasurementMap.put(jobUuid, carbonMeasurement);
    }

    public boolean containsKey(UUID jobUuid) {
        return this.initialMeasurementMap.containsKey(jobUuid);
    }

    public CarbonMeasurement getInitialMeasurement(UUID jobUuid) {
        return this.initialMeasurementMap.get(jobUuid);
    }

    public CarbonMeasurement getFinalMeasurement(UUID jobUuid) {
        return this.finalMeasurementMap.get(jobUuid);
    }

    public void putFinalMeasurementForJob(UUID jobUuid, CarbonMeasurement carbonMeasurement) {
        this.finalMeasurementMap.put(jobUuid, carbonMeasurement);
    }

    public InitialAndFinal<CarbonMeasurement> getInitialAndFinal(UUID jobUuid) {
        return new InitialAndFinal<>(this.initialMeasurementMap.get(jobUuid), this.finalMeasurementMap.get(jobUuid));
    }
}
