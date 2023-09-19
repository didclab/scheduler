package com.onedatashare.scheduler.services;

import com.hazelcast.core.HazelcastInstance;
import junit.framework.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

@SpringBootTest(properties = "spring.profiles.active=dev", classes = "com.rabbitmq.rabbitmqscheduler.")
public class JobSchedulerTest extends TestCase {

    JobScheduler testObj;

    @Autowired
    HazelcastInstance devHazelcastConfig;

    @Autowired
    RequestModifier requestModifier;
    @MockBean
    MessageSender messageSender;

    public void testCreateTestPojo(){
//        HazelcastInstance hazelcastInstance, RequestModifier requestModifier, MessageSender messageSender
        testObj = new JobScheduler(hazelcastInstance, requestModifier, messageSender);

        Assert.isTrue(testObj.jobIMap.size() == 0, "Default map is of size not 0??");

    }
}
