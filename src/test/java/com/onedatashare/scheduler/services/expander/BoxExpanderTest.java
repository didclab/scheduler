package com.onedatashare.scheduler.services.expander;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.credential.OAuthEndpointCredential;
import com.onedatashare.scheduler.services.expanders.BoxExpander;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BoxExpanderTest {

    private List<String> folders = new ArrayList<>();
    private List<String> files = new ArrayList<>();
    private EntityInfo pretendFile;
    BoxExpander testObj;


    @BeforeEach
    public void setUp() {
        folders = new ArrayList<>();
        files = new ArrayList<>();
        pretendFile = new EntityInfo();
        populateRoot();
    }

    public OAuthEndpointCredential oAuthEndpointCredentialWithDevToken() {
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential();
        //add your access token for testing
        //oAuthEndpointCredential.setToken(<token>);
        return oAuthEndpointCredential;
    }

    @Test
    public void testEmptyUserSelectedResources() {
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assertions.assertNotNull(rootDirExpanded);
        for (EntityInfo fileInfo : rootDirExpanded) {
            Assertions.assertNotNull(fileInfo);
            Assertions.assertTrue(fileInfo.getSize() > 0);
            Assertions.assertNotNull(fileInfo.getId());
        }
    }

    @Test
    public void testSelectedRootFile() {
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(createRootList(), "");
        Assertions.assertNotNull(rootDirExpanded);
        for (EntityInfo fileInfo : rootDirExpanded) {
            Assertions.assertNotNull(fileInfo);
            Assertions.assertTrue(fileInfo.getSize() > 0);
            Assertions.assertNotNull(fileInfo.getId());
        }
    }

    @Test
    public void testSelectedTwoRandomDirectories() {
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(createTwoDirExpansion(), "");
        Assertions.assertNotNull(rootDirExpanded);
        for (EntityInfo fileInfo : rootDirExpanded) {
            Assertions.assertNotNull(fileInfo);
            Assertions.assertTrue(fileInfo.getSize() > 0);
            Assertions.assertNotNull(fileInfo.getId());
        }
    }

    @Test
    public void testSelectedResourcesTwoDirsAndOneFile() {
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(expandTwoDirWithOtherFiles(), "");
        Assertions.assertNotNull(rootDirExpanded);
        for (EntityInfo fileInfo : rootDirExpanded) {
            Assertions.assertNotNull(fileInfo);
            Assertions.assertTrue(fileInfo.getSize() > 0);
            Assertions.assertNotNull(fileInfo.getId());
        }
    }

    @Test
    public void testDestinationChunkSizeTrivial() {
        List<EntityInfo> expandedFiles = new ArrayList<>();
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        Assertions.assertEquals(testObj.destinationChunkSize(expandedFiles, "", 64000), expandedFiles);
    }

    @Test
    public void testDestinationChunkSizeWithChunkSizeWithOneFile() {
        List<EntityInfo> expandedFiles = List.of(pretendFile);
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        Assertions.assertEquals(testObj.destinationChunkSize(expandedFiles, "", 64000), expandedFiles);
    }

    @Test
    public void testOneDir() {
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> input = this.testOneDirPoo();
        List<EntityInfo> fileInfos = testObj.expandedFileSystem(input, "");
        Assertions.assertNotNull(fileInfos);
    }

    @Test
    public void testDestinationChunkSizeBox() {
        List<EntityInfo> testList = List.of(pretendFile);
        int chunkSize = 100000;
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        testList = testObj.destinationChunkSize(testList, "0", chunkSize);
        Assertions.assertTrue(testList.get(0).getSize() != chunkSize);
    }

    public List<EntityInfo> testOneDirPoo() {
        List<EntityInfo> arrayList = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId(folders.get(0));
//        entityInfo.setPath("154484914681");
        arrayList.add(entityInfo);
        return arrayList;
    }

    public ArrayList<EntityInfo> createRootList() {
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo rootInfo = new EntityInfo();
        rootInfo.setPath("");
        rootInfo.setId("0");
        rootInfo.setSize(0);
        list.add(rootInfo);
        return list;
    }

    public ArrayList<EntityInfo> createTwoDirExpansion() {
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo nextInfoTwo = new EntityInfo();
        nextInfoTwo.setPath("");
        nextInfoTwo.setId(folders.get(0));
        nextInfoTwo.setSize(0);
        list.add(nextInfoTwo);
        if (folders.size() > 1) {
            EntityInfo nextInfo = new EntityInfo();
            nextInfo.setPath("");
            nextInfoTwo.setId(folders.get(1));
            nextInfo.setSize(0);
            list.add(nextInfo);
        }
        return list;
    }

    public ArrayList<EntityInfo> expandTwoDirWithOtherFiles() {
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo nextInfoTwo = new EntityInfo();
        nextInfoTwo.setPath("");
        nextInfoTwo.setId(folders.get(0));
        nextInfoTwo.setSize(0);
        list.add(nextInfoTwo);

        EntityInfo nextInfo = new EntityInfo();
        nextInfo.setPath("");
        nextInfo.setId(files.get(0));
        nextInfo.setSize(0);
        list.add(nextInfo);

        if (folders.size() > 1) {
            EntityInfo file = new EntityInfo();
            nextInfo.setPath("");
            nextInfo.setId(folders.get(1));
            nextInfo.setSize(0);
            list.add(file);
        }
        return list;
    }

    /**
     * populate all files and folders in the root
     */
    private void populateRoot() {
        boolean isPretendSet = false;
        BoxAPIConnection connection = new BoxAPIConnection(oAuthEndpointCredentialWithDevToken().getToken());
        BoxFolder temp = new BoxFolder(connection, "0");
        for (BoxItem.Info child : temp) {
            if (child instanceof BoxFolder.Info) {
                BoxFolder.Info folder = (BoxFolder.Info) child;
                folders.add(folder.getID());
            } else {
                BoxFile.Info file = (BoxFile.Info) child;
                files.add(file.getID());
                if (!isPretendSet) {
                    pretendFile.setId(file.getID());
                    pretendFile.setSize(file.getSize());
                    isPretendSet = true;
                }

            }
        }

    }

}
