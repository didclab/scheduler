package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class FTPExpanderTest extends TestCase {

    FTPExpander testObj;

    public AccountEndpointCredential testFTPCredential(){
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setAccountId("testuser@helloworld.com");
        accountEndpointCredential.setUsername("anonymous");
        accountEndpointCredential.setSecret("anonymous");
        accountEndpointCredential.setUri("ftp://speedtest.tele2.net");
        return accountEndpointCredential;
    }

    public void testlistAllFilesFromSpeedTest() {
        testObj = new FTPExpander();
        testObj.createClient(testFTPCredential());
        List<EntityInfo> fullFiles = testObj.expandedFileSystem(new ArrayList<>(),"/");
        for(EntityInfo file: fullFiles){
            System.out.println(file.toString());
        }
        Assert.isTrue(fullFiles.size() >0, "the amount of files on speed test tele2net");
    }

    public void testListingOnlyUploadDirectory(){
        testObj = new FTPExpander();
        testObj.createClient(testFTPCredential());
        createInfoList();
        List<EntityInfo> fullFiles = testObj.expandedFileSystem(createInfoList(),"/");
        Assert.isTrue(fullFiles.size() >0, "the amount of files on speed test tele2net");
    }

    public List<EntityInfo> createInfoList(){
        ArrayList<EntityInfo> listInfo = new ArrayList<>();
        EntityInfo testInfo = new EntityInfo();
        testInfo.setId("upload/");
        listInfo.add(testInfo);
        return listInfo;
    }
}