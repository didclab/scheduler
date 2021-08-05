package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class S3ExpanderTest extends TestCase {

    S3Expander testObj;

    public AccountEndpointCredential createTestCredentials() {
        String S3TESTKEY = System.getenv("AWS_JACOB_S3_KEY");
        String S3TESTSECRET = System.getenv("AWS_JACOB_S3_SECRET");
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setAccountId("hello@test.com");
        accountEndpointCredential.setUri("us-east-2:::jacobstestbucket");
        accountEndpointCredential.setUsername(S3TESTKEY);
        accountEndpointCredential.setSecret(S3TESTSECRET);
        return accountEndpointCredential;

    }

    public void testCreateClient() {
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        Assert.isTrue(testObj.s3Client != null, "The client is null somehow");
        Assert.isTrue(testObj.s3Client.getRegion().toString().equals("us-east-2"), "Region does not match the test credentials");
    }

    public void testExpandWholeBucket() {
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.isTrue(expandedBucketFiles.size() > 0, "The size was less than 0");
        for(int i = 0; i < expandedBucketFiles.size(); i++){
            System.out.println(expandedBucketFiles.get(i).toString());
        }
    }

    public void testOneFile(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setPath("100KB.zip");
        fileInfo.setId("100KB.zip");
        fileInfo.setSize(32);
        list.add(fileInfo);
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(list, "");
        Assert.isTrue(expandedBucketFiles.size() == 1, "The size should be one");
        for(int i = 0; i < expandedBucketFiles.size(); i++){
            System.out.println(expandedBucketFiles.get(i).toString());
        }

    }

    public void testExpandWholeBucketWithSlash() {
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(new ArrayList<>(), "/");
        Assert.isTrue(expandedBucketFiles.size() > 0, "The size was less than 0");
        for(int i = 0; i < expandedBucketFiles.size(); i++){
            System.out.println(expandedBucketFiles.get(i).toString());
        }
    }
}