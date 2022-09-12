package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class S3ExpanderTest extends TestCase {

    S3Expander testObj;

    public AccountEndpointCredential createTestCredentials() {
        String S3TESTKEY = "AKIASFB52FW73BTZEGY5";
        String S3TESTSECRET = "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";
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
    }

    public void testExpandUsersPrefix(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> selectedRes = new ArrayList<>();
        EntityInfo another = new EntityInfo();
        another.setPath("Users/");
        another.setId("Users/");
        selectedRes.add(another);
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(selectedRes, "");
        Assert.isTrue(expandedBucketFiles.size() == 2 , "The size of Users is 2 which it was not given out");

        /**
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setPath("100MB.zip");
        fileInfo.setId("100MB.zip");
        fileInfo.setSize(32);
        list.add(fileInfo);
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(list, "");
        Assert.isTrue(expandedBucketFiles.size() == 1, "The size should be one");
         **/

    }

    public void testExpandHelloWorldJacobTestBucketSlash(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> userSelectedFiles = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("helloworld/");
        entityInfo.setPath("helloworld/");
        userSelectedFiles.add(entityInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(userSelectedFiles, "/");
        assertEquals(27, files.size());
    }

    /**
     * This test fails b/c it includes the "/" as an entry. Not good :(
     */
    public void testExpandHelloWorldJacobTestBucketNoSlash(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> userSelectedFiles = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("helloworld/");
        entityInfo.setPath("helloworld/");
        userSelectedFiles.add(entityInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(userSelectedFiles, "");
        for(EntityInfo fileInfo : files){
            System.out.println(fileInfo.toString());
        }
        assertEquals(27, files.size());
    }


    public void testExpandWholeBucketWithSlash() {
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.isTrue(expandedBucketFiles.size() > 0, "The size was less than 0");
        for(int i = 0; i < expandedBucketFiles.size(); i++){
            System.out.println(expandedBucketFiles.get(i).toString());
        }
    }

    public void testExpandUsersAndTemp(){
        testObj = new S3Expander();
        testObj.createClient(createTestCredentials());
        ArrayList<EntityInfo> twoInfosToMove = new ArrayList<>();
        EntityInfo info = new EntityInfo();
        info.setId("temp/");
        info.setPath("temp/");
        EntityInfo another = new EntityInfo();
        another.setPath("Users/");
        another.setId("Users/");
        twoInfosToMove.add(info);
        twoInfosToMove.add(another);
        List<EntityInfo> expandedBucketFiles = testObj.expandedFileSystem(twoInfosToMove, "");
        Assert.isTrue(expandedBucketFiles.size() == 8, "the total files was not 8");
    }
}