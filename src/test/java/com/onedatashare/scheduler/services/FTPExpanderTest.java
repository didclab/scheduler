package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.services.expanders.FTPExpander;
import com.onedatashare.scheduler.model.EntityInfo;
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
        accountEndpointCredential.setUri("ftp://speedtest.tele2.net:21");
        return accountEndpointCredential;
    }

    public AccountEndpointCredential testNCBICredential(){
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setAccountId("testuser@helloworld.com");
        accountEndpointCredential.setUsername("anonymous");
        accountEndpointCredential.setSecret("anonymous");
        accountEndpointCredential.setUri("ftp://ftp.ncbi.nih.gov:21");
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

    public void testListingOneGB(){
        testObj = new FTPExpander();
        testObj.createClient(testNCBICredential());
        ArrayList<EntityInfo> selectedFolders = new ArrayList<>();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("1GB");
        fileInfo.setPath("1GB");
        selectedFolders.add(fileInfo);
        List<EntityInfo> fullFiles = testObj.expandedFileSystem(selectedFolders,"");
        for(EntityInfo file: fullFiles){
            System.out.println(file.toString());
        }

        Assert.isTrue(fullFiles.size() > 0, "the amount of files on speed test tele2net");
    }

    public void testListingUploadAndHundreMB(){
        testObj = new FTPExpander();
        testObj.createClient(testFTPCredential());
        List<EntityInfo> fullFiles = testObj.expandedFileSystem(createInfoList(),"");
        for(EntityInfo file: fullFiles){
            System.out.println(file.toString());
        }

        Assert.isTrue(fullFiles.size() > 0, "the amount of files on speed test tele2net");
    }

    public EntityInfo hundredMBFile(){
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("100MB.zip");
        fileInfo.setSize(104857600);
        fileInfo.setPath("/100MB.zip");
        return fileInfo;
    }

    public EntityInfo uploadDirectory(){
        EntityInfo testInfo = new EntityInfo();
        testInfo.setId("upload/");
        testInfo.setPath("upload/");
        return testInfo;
    }

    public List<EntityInfo> createInfoList(){
        ArrayList<EntityInfo> listInfo = new ArrayList<>();
        listInfo.add(uploadDirectory());
        listInfo.add(hundredMBFile());
        return listInfo;
    }
}