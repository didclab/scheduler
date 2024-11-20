package com.onedatashare.scheduler.mapper;

import com.hazelcast.map.MapStore;
import com.onedatashare.scheduler.model.RequestFromODS;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class ScheduledJobsMapStore implements MapStore<UUID, RequestFromODS> {
    @Override
    public void store(UUID key, RequestFromODS value) {
        
    }

    @Override
    public void storeAll(Map<UUID, RequestFromODS> map) {

    }

    @Override
    public void delete(UUID key) {

    }

    @Override
    public void deleteAll(Collection<UUID> keys) {

    }

    @Override
    public RequestFromODS load(UUID key) {
        return null;
    }

    @Override
    public Map<UUID, RequestFromODS> loadAll(Collection<UUID> keys) {
        return null;
    }

    @Override
    public Iterable<UUID> loadAllKeys() {
        return null;
    }
}
