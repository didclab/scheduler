package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Future enhancements-
 * - make bucket name configurable
 * - avoid hardcoded file names in tests
 * - for testing, add file and remove it
 */
public class S3ExpanderTest extends TestCase {

    S3Expander testObj;

    public AccountEndpointCredential createTestCredentials() {
        String S3TESTKEY = "";
        String S3TESTSECRET = "";
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setAccountId("hello@test.com");
        accountEndpointCredential.setUri("us-east-2:::odscerts");
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
    }

    /**
     * This test fails b/c it includes the "/" as an entry. Not good :(
     */
    public void testExpandHelloWorldJacobTestBucketNoSlash(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> userSelectedFiles = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("transferCDB_Certs/");
        entityInfo.setPath("transferCDB_Certs/");
        userSelectedFiles.add(entityInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(userSelectedFiles, "");
        for(EntityInfo fileInfo : files){
            System.out.println("->" + fileInfo.toString());
        }
        assertEquals(3, files.size());
    }


    public void testExpandWholeBucketBasePathWithSlash() {
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(new ArrayList<>(), "/");
        Assert.isTrue(expandedBucketFiles.size() > 0, "The size was less than 0");
    }

    /**
     * Test absolute path in entity info
     */
    public void testOneFileNoBasePath(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setPath("transferCDB_Certs/ca.crt");
        fileInfo.setId("transferCDB_Certs/ca.crt");
        list.add(fileInfo);
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(list, "");
        Assert.isTrue(expandedBucketFiles.size() == 1, "The size should be one");
    }

    /**
     * Test relative path in entity info, basePath with prefix
     */
    public void testOneFileWithBasePath(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setPath("ca.crt");
        fileInfo.setId("ca.crt");
        list.add(fileInfo);
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(list, "/transferCDB_Certs/");
        Assert.isTrue(expandedBucketFiles.size() == 1, "The size should be one");
    }

    public void testDestinationChunkSizeGoFile(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        List<EntityInfo> testList = testObj.destinationChunkSize(goFile(), "", 1000);
        Assert.isTrue(testList.get(0).getChunkSize() == 10000000, "The proper chunk size was not set");
    }
    public void testDestinationChunkSizeGoFileLargeChunkSize(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        List<EntityInfo> testList = testObj.destinationChunkSize(goFile(), "", 10000000);
        Assert.isTrue(testList.get(0).getChunkSize() == 10000000, "The proper chunk size was not set");
    }

    public void testExpandPrefix(){
        testObj = new S3Expander();
        AccountEndpointCredential cred = createTestCredentials();
        testObj.createClient(createTestCredentials());
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setPath("transfer_service_secrets/");
        entityInfo.setId("transfer_service_secrets/");
        List<EntityInfo> folder = new ArrayList<>();
        folder.add(entityInfo);
        List<EntityInfo> testList = testObj.expandedFileSystem(folder, "/");
        for(EntityInfo fileInfo : testList){
            System.out.println(fileInfo.toString());
        }
        Assert.isTrue(testList.size() > 3, "Should have more than 3 files");
    }

    public void testExpandTwoPrefix(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        EntityInfo folder1 = new EntityInfo();
        folder1.setPath("transfer_service_secrets/");
        folder1.setId("transfer_service_secrets/");
        EntityInfo folder2 = new EntityInfo();
        folder2.setPath("transferCDB_Certs/");
        folder2.setId("transferCDB_Certs/");

        List<EntityInfo> folder = new ArrayList<>();
        folder.add(folder1);
        folder.add(folder2);
        List<EntityInfo> testList = testObj.expandedFileSystem(folder, "/");
        for(EntityInfo fileInfo : testList){
            System.out.println(fileInfo.toString());
        }
        Assert.isTrue(testList.size() == 8, "There should be 8 files we are listing from two directories");

    }

    public List<EntityInfo> goFile(){
        List<EntityInfo> testList = new ArrayList<>();
        EntityInfo goFile = new EntityInfo();
        goFile.setId("go1.16beta1.darwin-amd64.tar");
        goFile.setSize(411944960);
        testList.add(goFile);
        return testList;
    }

}