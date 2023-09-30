package com.onedatashare.scheduler.services;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.onedatashare.scheduler.config.CacheConfig;
import com.onedatashare.scheduler.model.FileDestination;
import com.onedatashare.scheduler.model.FileSource;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.model.UserTransferOptions;
import junit.framework.TestCase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobSchedulerTest extends TestCase {

    JobScheduler testObj;

    HazelcastInstance hazelcastInstance;

    @Override
    protected void setUp() throws Exception {
        CacheConfig cacheConfig = new CacheConfig();
        Config devConfig = cacheConfig.devHazelcastConfig();
        this.hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(devConfig);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @MockBean
    MessageSender messageSender;

    @MockBean
    RequestModifier requestModifier;


    public void testCreateTestPojo() {
//        HazelcastInstance hazelcastInstance, RequestModifier requestModifier, MessageSender messageSender
        testObj = new JobScheduler(hazelcastInstance, requestModifier, messageSender);
        Assert.isTrue(testObj.jobIMap.isEmpty(), "Default map is of size not 0??");
    }

    public void testAddOneNullEntryToHazelcast() {
        testObj = new JobScheduler(hazelcastInstance, requestModifier, messageSender);
        testObj.saveScheduledJob(null, LocalDateTime.now());
        Assert.isTrue(testObj.jobIMap.isEmpty(), "Added one null entry and there should be size 0");
    }

    public void testAddOneRealEntryToHazelcast() {
        testObj = new JobScheduler(hazelcastInstance, requestModifier, messageSender);
        RequestFromODSDTO request = new RequestFromODSDTO();
        request.setOwnerId("test@email.com");
        request.setSource(new FileSource());
        request.setDestination(new FileDestination());
        request.setTransferNodeName("");
        request.setOptions(new UserTransferOptions());
        testObj.saveScheduledJob(request, LocalDateTime.now());
        Assert.isTrue(testObj.jobIMap.size() == 1, "Added one Valid entry and there should be size 0");
    }

    public void testRemoveOneEntryToHazelcast() {
        testObj = new JobScheduler(hazelcastInstance, requestModifier, messageSender);
        RequestFromODSDTO request = new RequestFromODSDTO();
        request.setOwnerId("test@email.com");
        request.setSource(new FileSource());
        request.setDestination(new FileDestination());
        request.setTransferNodeName("");
        request.setOptions(new UserTransferOptions());
        UUID id = testObj.saveScheduledJob(request, LocalDateTime.now());
        Assert.isTrue(testObj.jobIMap.size() == 1, "Added one Valid entry and there should be size 0");
        System.out.println("Id: " + id.toString());
        testObj.deleteScheduledJob(id);
        Assert.isTrue(testObj.jobIMap.isEmpty(), "Tried to delete the only entry so size should be 0");
    }

    public void testRemoteOneNoneExistentEntry(){
        testObj = new JobScheduler(hazelcastInstance, requestModifier, messageSender);
        RequestFromODSDTO request = new RequestFromODSDTO();
        request.setOwnerId("test@email.com");
        request.setSource(new FileSource());
        request.setDestination(new FileDestination());
        request.setTransferNodeName("");
        request.setOptions(new UserTransferOptions());
        UUID id = testObj.saveScheduledJob(request, LocalDateTime.now());
        Assert.isTrue(testObj.jobIMap.size() == 1, "Added one Valid entry and there should be size 0");

        System.out.println("Id: " + id.toString());
        testObj.deleteScheduledJob(id);
        Assert.isTrue(testObj.jobIMap.isEmpty(), "Tried to delete the only entry so size should be 0");
        testObj.deleteScheduledJob(id);
        Assert.isTrue(testObj.jobIMap.isEmpty(), "Tried to delete an entry on an empty map");

    }

}
