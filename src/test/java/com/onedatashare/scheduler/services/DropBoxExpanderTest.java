package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.credential.OAuthEndpointCredential;
import com.onedatashare.scheduler.services.expanders.DropBoxExpander;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class DropBoxExpanderTest extends TestCase {

    DropBoxExpander testObj;

    public OAuthEndpointCredential devToken(){
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential();
        oAuthEndpointCredential.setToken("sl.A_K-hQxIens5a7w82VXq2P1WekpuboaS5fLq4YfPLSF76k1fyBwFxqnR6D4kvnn8OMpqCEdw-Xc5x03P4FuJBT2ekcQ8q7v9sruwM6yp2sWhxjRyFQ-BV4ikvci9LRWmnyd6uY-0");
        return oAuthEndpointCredential;
    }

    public void testCreateClient() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
    }

    public void testExpandedEntireDropBoxAccount() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(null, null);
        Assert.assertTrue(fileList.size() > 0);
        for(EntityInfo fileInfo : fileList){
            System.out.println(fileInfo.toString());
        }
    }

    public void testExpandSpecificDir(){
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(selectOneFolderWithOneFile(), "");
        for(EntityInfo fileInfo : fileList){
            System.out.println(fileInfo.toString());
        }
        Assert.assertEquals(1, fileList.size());
    }

    public void testExpandSpecificTwoDirs(){
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(selectTwoFoldersWithOneFileEach(), "");
        for(EntityInfo fileInfo : fileList){
            System.out.println(fileInfo.toString());
        }
        Assert.assertEquals(2, fileList.size());
    }

    public void testExpandSpecificThreeDirs(){
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(selectThreeFoldersWithOneFileEach(), "");
        for(EntityInfo fileInfo : fileList){
            System.out.println(fileInfo.toString());
        }
        Assert.assertEquals(3, fileList.size());
    }

    public ArrayList<EntityInfo> selectTwoFoldersWithOneFileEach(){
        EntityInfo oneFile = new EntityInfo();
        EntityInfo twoFile = new EntityInfo();
        oneFile.setPath("/hello");
        oneFile.setId("");
        oneFile.setSize(0);
        twoFile.setSize(0);
        twoFile.setPath("/nested/test");
        twoFile.setId("");
        ArrayList<EntityInfo> list = new ArrayList();
        list.add(oneFile);
        list.add(twoFile);
        return list;
    }
    public List<EntityInfo> selectOneFolderWithOneFile(){
        EntityInfo oneFile = new EntityInfo();
        oneFile.setPath("/hello");
        oneFile.setId("");
        oneFile.setSize(0);
        ArrayList<EntityInfo> list = new ArrayList();
        list.add(oneFile);
        return list;
    }


    public List<EntityInfo> selectThreeFoldersWithOneFileEach(){
        EntityInfo oneFile = new EntityInfo();
        EntityInfo twoFile = new EntityInfo();
        EntityInfo threeFile = new EntityInfo();
        oneFile.setPath("/hello");
        oneFile.setId("");
        oneFile.setSize(0);
        twoFile.setSize(0);
        twoFile.setPath("/nested/test");
        twoFile.setId("");
        threeFile.setId("");
        threeFile.setPath("/testing");
        threeFile.setSize(0);
        ArrayList<EntityInfo> list = new ArrayList();
        list.add(oneFile);
        list.add(twoFile);
        list.add(threeFile);
        return list;
    }

    public void testDestinationChunkSizeSmallChunkSize() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> expandedFiles = testObj.destinationChunkSize(goFile(), "", 1000);
        Assert.assertTrue("DropBox has minimum chunk size of 4MB",expandedFiles.get(0).getChunkSize() == 4000000);
    }

    public void testDestinationChunkSizeLargeChunkSize(){
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> expandedFiles = testObj.destinationChunkSize(goFile(), "", 10000000);
        Assert.assertTrue("DropBox has minimum chunk size of 4MB",expandedFiles.get(0).getChunkSize() == 10000000);
    }

    public List<EntityInfo> goFile(){
        List<EntityInfo> testList = new ArrayList<>();
        EntityInfo goFile = new EntityInfo();
        goFile.setId("(Bert Dodson) Keys to Drawing.pdf");
        goFile.setSize(151551496);
        testList.add(goFile);
        return testList;
    }
}
