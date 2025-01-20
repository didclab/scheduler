package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.onedatashare.scheduler.model.FileDestination;
import com.onedatashare.scheduler.model.FileSource;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.model.UserTransferOptions;
import com.onedatashare.scheduler.services.maps.TransferSchedulerMapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobSchedulerTest {

    JobScheduler testObj;

    HazelcastInstance hazelcastInstance;

    public JobSchedulerTest() {
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    @MockBean
    MessageSender messageSender;

    @MockBean
    RequestModifier requestModifier;


    @MockBean
    TransferSchedulerMapService transferSchedulerMapService;

    @MockBean
    FileTransferNodeDiscovery fileTransferNodeDiscovery;

    String testUserEmail = "test@gmail.com";

    @Test
    public void testOnStartMapIsEmpty() throws JsonProcessingException {
        //TransferSchedulerMapService transferSchedulerMapService, RequestModifier requestModifier, MessageSender messageSender
        testObj = new JobScheduler(fileTransferNodeDiscovery, transferSchedulerMapService, requestModifier, messageSender);
        Assert.isTrue(testObj.listScheduledJobs("").isEmpty(), "Default map is of size not 0??");
    }

    @Test
    public void testAddOneNullEntryToHazelcast() throws JsonProcessingException {
        testObj = new JobScheduler(fileTransferNodeDiscovery, transferSchedulerMapService, requestModifier, messageSender);
        testObj.saveScheduledJob(null, LocalDateTime.now());
        Assert.isTrue(testObj.listScheduledJobs("").isEmpty(), "Added one null entry and there should be size 0");
    }

    @Test
    public void testAddOneRealEntryToHazelcast() throws JsonProcessingException {
        testObj = new JobScheduler(fileTransferNodeDiscovery, transferSchedulerMapService, requestModifier, messageSender);
        RequestFromODSDTO request = new RequestFromODSDTO();
        request.setOwnerId(this.testUserEmail);
        request.setSource(new FileSource());
        request.setDestination(new FileDestination());
        request.setTransferNodeName("");
        request.setOptions(new UserTransferOptions());
        testObj.saveScheduledJob(request, LocalDateTime.now());
        Assert.isTrue(testObj.fileTransferScheduleMap.queryUserScheduledJobs(this.testUserEmail).size() == 1, "Added one Valid entry and there should be size 0");
    }

    @Test
    public void testRemoveOneEntryToHazelcast() throws JsonProcessingException {
        testObj = new JobScheduler(fileTransferNodeDiscovery, transferSchedulerMapService, requestModifier, messageSender);
        RequestFromODSDTO request = new RequestFromODSDTO();
        request.setOwnerId("test@email.com");
        request.setSource(new FileSource());
        request.setDestination(new FileDestination());
        request.setTransferNodeName("");
        request.setOptions(new UserTransferOptions());
        UUID id = testObj.saveScheduledJob(request, LocalDateTime.now());
        Assert.isTrue(testObj.listScheduledJobs(this.testUserEmail).size() == 1, "Added one Valid entry and there should be size 0");
        System.out.println("Id: " + id.toString());
        testObj.deleteScheduledJob(id);
        Assert.isTrue(testObj.listScheduledJobs(this.testUserEmail).isEmpty(), "Tried to delete the only entry so size should be 0");
    }

    @Test
    public void testRemoteOneNoneExistentEntry() throws JsonProcessingException {
        testObj = new JobScheduler(fileTransferNodeDiscovery, transferSchedulerMapService, requestModifier, messageSender);
        RequestFromODSDTO request = new RequestFromODSDTO();
        request.setOwnerId("test@email.com");
        request.setSource(new FileSource());
        request.setDestination(new FileDestination());
        request.setTransferNodeName("");
        request.setOptions(new UserTransferOptions());
        UUID id = testObj.saveScheduledJob(request, LocalDateTime.now());
        Assert.isTrue(testObj.listScheduledJobs(this.testUserEmail).size() == 1, "Added one Valid entry and there should be size 0");

        System.out.println("Id: " + id.toString());
        testObj.deleteScheduledJob(id);
        Assert.isTrue(testObj.listScheduledJobs(this.testUserEmail).isEmpty(), "Tried to delete the only entry so size should be 0");
        testObj.deleteScheduledJob(id);
        Assert.isTrue(testObj.listScheduledJobs(this.testUserEmail).isEmpty(), "Tried to delete an entry on an empty map");

    }

}
