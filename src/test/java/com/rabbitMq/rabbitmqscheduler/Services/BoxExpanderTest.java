package com.rabbitMq.rabbitmqscheduler.Services;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class BoxExpanderTest extends TestCase {

    private List<String> folders = new ArrayList<>();
    private List<String> files = new ArrayList<>();
    private EntityInfo pretendFile;
    BoxExpander testObj;


    @Before
    public void setUp(){
        folders = new ArrayList<>();
        files = new ArrayList<>();
        pretendFile = new EntityInfo();
        populateRoot();
    }

    public OAuthEndpointCredential oAuthEndpointCredentialWithDevToken(){
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential();
        //add your access token for testing
        //oAuthEndpointCredential.setToken(<token>);
        return oAuthEndpointCredential;
    }

    public void testEmptyUserSelectedResources(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> rootDirExpanded = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.assertNotNull(rootDirExpanded);
        for(EntityInfo fileInfo: rootDirExpanded){
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
        }
    }

    public void testDestinationChunkSizeTrivial() {
        List<EntityInfo> expandedFiles = new ArrayList<>();
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        Assert.assertEquals(testObj.destinationChunkSize(expandedFiles, "", 64000), expandedFiles);
    }

    public void testDestinationChunkSizeWithChunkSizeWithOneFile() {
        List<EntityInfo> expandedFiles = List.of(pretendFile);
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        Assert.assertEquals(testObj.destinationChunkSize(expandedFiles, "", 64000), expandedFiles);
    }

    public void testOneDir(){
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        List<EntityInfo> input = this.testOneDirPoo();
        List<EntityInfo> fileInfos = testObj.expandedFileSystem(input, "");
        Assert.assertNotNull(fileInfos);
    }

    public void testDestinationChunkSizeBox(){
        List<EntityInfo> testList = List.of(pretendFile);
        int chunkSize = 100000;
        testObj = new BoxExpander();
        testObj.createClient(oAuthEndpointCredentialWithDevToken());
        testList = testObj.destinationChunkSize(testList, "0", chunkSize);
        Assert.assertTrue("The original chunkSize is not right for uploading to box",testList.get(0).getSize() != chunkSize);
    }

    public List<EntityInfo> testOneDirPoo(){
        List<EntityInfo> arrayList = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId(folders.get(0));
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
        nextInfoTwo.setId(folders.get(0));
        nextInfoTwo.setSize(0);
        list.add(nextInfoTwo);
        if(folders.size() > 1) {
            EntityInfo nextInfo = new EntityInfo();
            nextInfo.setPath("");
            nextInfoTwo.setId(folders.get(1));
            nextInfo.setSize(0);
            list.add(nextInfo);
        }
        return list;
    }

    public ArrayList<EntityInfo> expandTwoDirWithOtherFiles(){
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

        if(folders.size() > 1) {
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
    private void populateRoot(){
        boolean isPretendSet = false;
        BoxAPIConnection connection = new BoxAPIConnection(oAuthEndpointCredentialWithDevToken().getToken());
        BoxFolder temp = new BoxFolder(connection, "0");
        for(BoxItem.Info child : temp){
            if(child instanceof BoxFolder.Info){
                BoxFolder.Info folder = (BoxFolder.Info) child;
                folders.add(folder.getID());
            }else{
                BoxFile.Info file = (BoxFile.Info) child;
                files.add(file.getID());
                if(!isPretendSet) {
                    pretendFile.setId(file.getID());
                    pretendFile.setSize(file.getSize());
                    isPretendSet = true;
                }

            }
        }

    }

}
