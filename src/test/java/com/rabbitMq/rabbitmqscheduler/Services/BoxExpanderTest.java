package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class BoxExpanderTest extends TestCase {

    BoxExpander testObj;

    public OAuthEndpointCredential oAuthEndpointCredentialWithDevToken(){
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential();
        return oAuthEndpointCredential;
    }

    public void testEmptyUserSelectedResources(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.assertNotNull(rootDirExpanded);
        for(EntityInfo fileInfo: rootDirExpanded){
            System.out.println(fileInfo.toString());
            Assert.assertNotNull(fileInfo);
            Assert.assertTrue(fileInfo.getSize() > 0);
            Assert.assertNotNull(fileInfo.getId());
        }
    }

    public void testSelectedRootFile(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(createRootList(), "");
        Assert.assertNotNull(rootDirExpanded);
        for(EntityInfo fileInfo : rootDirExpanded){
            Assert.assertNotNull(fileInfo);
            Assert.assertTrue(fileInfo.getSize() > 0);
            Assert.assertNotNull(fileInfo.getId());
            System.out.println(fileInfo);
        }
    }

    public void testSelectedTwoRandomDirectories(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(createTwoDirExpansion(), "");
        Assert.assertNotNull(rootDirExpanded);
        for(EntityInfo fileInfo : rootDirExpanded){
            Assert.assertNotNull(fileInfo);
            Assert.assertTrue(fileInfo.getSize() > 0);
            Assert.assertNotNull(fileInfo.getId());
            System.out.println(fileInfo);
        }
    }

    public void testSelectedResourcesTwoDirsAndOneFile(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(expandTwoDirWithOtherFiles(), "");
        Assert.assertNotNull(rootDirExpanded);
        for(EntityInfo fileInfo : rootDirExpanded){
            Assert.assertNotNull(fileInfo);
            Assert.assertTrue(fileInfo.getSize() > 0);
            Assert.assertNotNull(fileInfo.getId());
            System.out.println(fileInfo);
        }
    }

    public void testDestinationChunkSizeTrivial() {
        List<EntityInfo> expandedFiles = new ArrayList<>();
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        Assert.assertEquals(testObj.destinationChunkSize(expandedFiles, "", 64000), expandedFiles);
    }

    public void testDestinationChunkSizeWithChunkSizeWithOneFile() {
        List<EntityInfo> expandedFiles = selectOnePretendFile();
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        Assert.assertEquals(testObj.destinationChunkSize(expandedFiles, "", 64000), expandedFiles);
    }

    public void testOneDir(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> fileInfos = testObj.expandedFileSystem(this.testOneDirPoo(), "");
        Assert.assertEquals(1, fileInfos.size());
        Assert.assertEquals("847290704115", fileInfos.get(0).getId());
        System.out.println(fileInfos.get(0).toString());
    }

    public void testDestinationChunkSizeBox(){
        List<EntityInfo> testList = selectOnePretendFile();
        int chunkSize = 100000;
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        testList = testObj.destinationChunkSize(testList, "0", chunkSize);
        Assert.assertTrue("The original chunkSize is not right for uploading to box",testList.get(0).getSize() != chunkSize);
        System.out.println(testList.get(0).getChunkSize());
    }

    public List<EntityInfo> testOneDirPoo(){
        List<EntityInfo> arrayList = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("154484914681");
//        entityInfo.setPath("154484914681");
        arrayList.add(entityInfo);
        return arrayList;
    }

    public ArrayList<EntityInfo> createRootList(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo rootInfo = new EntityInfo();
        rootInfo.setPath("");
        rootInfo.setId("0");
        rootInfo.setSize(0);
        list.add(rootInfo);
        return list;
    }

    public ArrayList<EntityInfo> createTwoDirExpansion(){
        ArrayList<EntityInfo> list = new ArrayList<>();

        EntityInfo nextInfoTwo = new EntityInfo();
        nextInfoTwo.setPath("");
        nextInfoTwo.setId("138759045640");
        nextInfoTwo.setSize(0);
        list.add(nextInfoTwo);

        EntityInfo nextInfo = new EntityInfo();
        nextInfo.setPath("");
        nextInfo.setId("103134722568");
        nextInfo.setSize(0);
        list.add(nextInfo);
        return list;
    }

    public ArrayList<EntityInfo> expandTwoDirWithOtherFiles(){
        ArrayList<EntityInfo> list = new ArrayList<>();

        EntityInfo nextInfoTwo = new EntityInfo();
        nextInfoTwo.setPath("");
        nextInfoTwo.setId("138758736572");
        nextInfoTwo.setSize(0);
        list.add(nextInfoTwo);

        EntityInfo nextInfo = new EntityInfo();
        nextInfo.setPath("");
        nextInfo.setId("138758865195");
        nextInfo.setSize(0);
        list.add(nextInfo);

        EntityInfo file = new EntityInfo();
        nextInfo.setPath("");
        nextInfo.setId("819408674349");
        nextInfo.setSize(0);
        list.add(file);
        return list;
    }

    public ArrayList<EntityInfo> selectOnePretendFile(){
        ArrayList<EntityInfo> fileInfo = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("819400248751");
        entityInfo.setPath("0");
        entityInfo.setSize(411944960);
        fileInfo.add(entityInfo);
        return fileInfo;
    }


}
