package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import junit.framework.TestCase;
import org.junit.Assert;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

public class BoxExpanderTest extends TestCase {

    BoxExpander testObj;

    public OAuthEndpointCredential oAuthEndpointCredentialWithDevToken(){
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential("testuserId");
        oAuthEndpointCredential.setToken("lb0xaHpyPb9RTYpyKYumxLc1RUoXPtgd");//this is temporary dev token
        return oAuthEndpointCredential;
    }

    public void testEmptyUserSelectedResources(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.assertNotNull(rootDirExpanded);
        for(EntityInfo fileInfo: rootDirExpanded){
            System.out.println(fileInfo.getId());
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

}
