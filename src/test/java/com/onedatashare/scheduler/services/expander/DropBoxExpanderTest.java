package com.onedatashare.scheduler.services.expander;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.credential.OAuthEndpointCredential;
import com.onedatashare.scheduler.services.expanders.DropBoxExpander;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DropBoxExpanderTest {

    DropBoxExpander testObj;

    public OAuthEndpointCredential devToken() {
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential();
        oAuthEndpointCredential.setToken("sl.A_K-hQxIens5a7w82VXq2P1WekpuboaS5fLq4YfPLSF76k1fyBwFxqnR6D4kvnn8OMpqCEdw-Xc5x03P4FuJBT2ekcQ8q7v9sruwM6yp2sWhxjRyFQ-BV4ikvci9LRWmnyd6uY-0");
        return oAuthEndpointCredential;
    }

    public void testCreateClient() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
    }

    @Test
    public void testExpandedEntireDropBoxAccount() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(null, null);
        Assertions.assertTrue(fileList.size() > 0);
        for (EntityInfo fileInfo : fileList) {
            System.out.println(fileInfo.toString());
        }
    }

    @Test
    public void testExpandSpecificDir() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(selectOneFolderWithOneFile(), "");
        for (EntityInfo fileInfo : fileList) {
            System.out.println(fileInfo.toString());
        }
        Assertions.assertEquals(1, fileList.size());
    }

    @Test
    public void testExpandSpecificTwoDirs() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(selectTwoFoldersWithOneFileEach(), "");
        for (EntityInfo fileInfo : fileList) {
            System.out.println(fileInfo.toString());
        }
        Assertions.assertEquals(2, fileList.size());
    }

    @Test
    public void testExpandSpecificThreeDirs() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> fileList = testObj.expandedFileSystem(selectThreeFoldersWithOneFileEach(), "");
        for (EntityInfo fileInfo : fileList) {
            System.out.println(fileInfo.toString());
        }
        Assertions.assertEquals(3, fileList.size());
    }

    public ArrayList<EntityInfo> selectTwoFoldersWithOneFileEach() {
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

    public List<EntityInfo> selectOneFolderWithOneFile() {
        EntityInfo oneFile = new EntityInfo();
        oneFile.setPath("/hello");
        oneFile.setId("");
        oneFile.setSize(0);
        ArrayList<EntityInfo> list = new ArrayList();
        list.add(oneFile);
        return list;
    }


    public List<EntityInfo> selectThreeFoldersWithOneFileEach() {
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

    @Test
    public void testDestinationChunkSizeSmallChunkSize() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> expandedFiles = testObj.destinationChunkSize(goFile(), "", 1000);
        Assertions.assertTrue(expandedFiles.get(0).getChunkSize() == 4000000);
    }

    @Test
    public void testDestinationChunkSizeLargeChunkSize() {
        testObj = new DropBoxExpander();
        testObj.createClient(devToken());
        List<EntityInfo> expandedFiles = testObj.destinationChunkSize(goFile(), "", 10000000);
        Assertions.assertTrue(expandedFiles.get(0).getChunkSize() == 10000000);
    }

    public List<EntityInfo> goFile() {
        List<EntityInfo> testList = new ArrayList<>();
        EntityInfo goFile = new EntityInfo();
        goFile.setId("(Bert Dodson) Keys to Drawing.pdf");
        goFile.setSize(151551496);
        testList.add(goFile);
        return testList;
    }
}
