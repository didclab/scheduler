package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.services.expanders.MinioExpander;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class MinioExpanderTest extends TestCase {

    MinioExpander testObj;

    public AccountEndpointCredential createCredential() {
        AccountEndpointCredential credential = new AccountEndpointCredential();
        credential.setUsername("admin");
        credential.setSecret("Minio@123");
        credential.setCustomEndpoint("http://localhost:9000");
        credential.setUri("us-east-1:::test-bucket");
        return credential;
    }


    public void testConnection() {
        try {
            testObj = new MinioExpander();
            testObj.createClient(this.createCredential());
            // If createClient() completes without throwing an exception, connection is successful
            Assert.isTrue(true, "MinIO connection successful");
        } catch (Exception e) {
            Assert.isTrue(false, "Failed to connect to MinIO: " + e.getMessage());
        }
    }

    public void testExpandRoot() {
        testObj = new MinioExpander();
        testObj.createClient(this.createCredential());
        List<EntityInfo> infoList = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.isTrue(!infoList.isEmpty(), "file info list turned up empty");

        for (EntityInfo fileInfo : infoList) {
            Assert.isTrue(fileInfo != null, "file info turned up null");
            Assert.isTrue(fileInfo.getSize() >= 0, "file size must be greater than or equal to 0");
            Assert.isTrue(fileInfo.getId() != null, "file id turned up null");
            Assert.isTrue(!fileInfo.getId().isEmpty(), "file id is empty");
            Assert.isTrue(fileInfo.getPath() != null, "file path is null");
            Assert.isTrue(!fileInfo.getPath().isEmpty(), "file path is empty");
            System.out.println(fileInfo.toString());
        }
    }

    public void testExpandTestFolder() {
        testObj = new MinioExpander();
        testObj.createClient(this.createCredential());
        List<EntityInfo> infoList = testObj.expandedFileSystem(filesToExpand(), "test-folder/");
        Assert.isTrue(!infoList.isEmpty(), "file info list --->  empty");

        for (EntityInfo fileInfo : infoList) {
            Assert.isTrue(fileInfo != null, "file info turned up null");
            Assert.isTrue(fileInfo.getSize() >= 0, "file size must be greater than or equal to 0");
            Assert.isTrue(fileInfo.getId() != null, "file id --> null");
            Assert.isTrue(!fileInfo.getId().isEmpty(), "file id is empty");
            Assert.isTrue(fileInfo.getPath() != null, "file path is null");
            Assert.isTrue(!fileInfo.getPath().isEmpty(), "file path is empty");
            Assert.isTrue(fileInfo.getPath().startsWith("test-folder/"), "File path should be in test folder");
            System.out.println(fileInfo.toString());
        }
    }

    public List<EntityInfo> filesToExpand() {
        List<EntityInfo> fileInfo = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("test-folder");
        entityInfo.setPath("test-folder/");
        fileInfo.add(entityInfo);
        return fileInfo;
    }
}
