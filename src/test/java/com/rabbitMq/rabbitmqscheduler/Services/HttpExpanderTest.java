package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class HttpExpanderTest extends TestCase {
    HttpExpander testObj;

    public AccountEndpointCredential credential(){
        AccountEndpointCredential cred = new AccountEndpointCredential();
        cred.setAccountId("testHttpServer");
        cred.setUri("http://129.114.109.132:80");
        return cred;
    }

    public void testNoCurrentDirectory(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        List<EntityInfo> files = testObj.expandedFileSystem(new ArrayList<>(), "");
        for(EntityInfo fileInfo : files){
            Assert.assertTrue(!fileInfo.getId().equals("..")); //make sure no fileInfo is directory above
            Assert.assertTrue(!fileInfo.getPath().endsWith("..")); //make sure not path ends with directory above
            Assert.assertTrue(!fileInfo.getId().equals(".")); //make sure no file id is the current directory
            System.out.println(fileInfo.toString());
        }
    }

    public void testParallelDirectoryOneFiles(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> directoryToExpand = new ArrayList<>();
        directoryToExpand.add(parallelFilesDir());
        List<EntityInfo> files = testObj.expandedFileSystem(directoryToExpand, "");
        Assert.assertTrue(files.size() == 1);
        Assert.assertTrue(files.get(0).getId().equals("montyParallel.dmg"));
        Assert.assertEquals(4294967296L, files.get(0).getSize());
    }

    public void testHelloWorldDirTwoFiles(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> directoryToExpand = new ArrayList<>();
        directoryToExpand.add(helloWorldTwoFiles());
        List<EntityInfo> files = testObj.expandedFileSystem(directoryToExpand, "");
        Assert.assertTrue(files.size() == 2);
        for(EntityInfo fileInfo : files){
            Assert.assertTrue(!fileInfo.getId().equals(".."));
            Assert.assertTrue(!fileInfo.getId().equals("."));
        }
        Assert.assertTrue(files.get(0).getId().equals("monty-7.dmg"));
        Assert.assertTrue(files.get(1).getId().equals("monty-7.dmg"));
        Assert.assertEquals(1073741824, files.get(0).getSize());
        Assert.assertEquals(1073741824, files.get(1).getSize());
    }

    public void testNestedTwoLevels(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> selectedFolders = new ArrayList<>();
        selectedFolders.add(anotherLayer());
        selectedFolders.add(parallelFilesDir());
        List<EntityInfo> fInfo = testObj.expandedFileSystem(selectedFolders, "");
        for(EntityInfo fileInfo : fInfo){
            if(fileInfo.getId().equals("montyParallel.dmg")){
                Assert.assertEquals(4294967296L, fileInfo.getSize());
            }
            if(fileInfo.getId().equals("monty-7.dmg")){
                Assert.assertEquals(1073741824L, fileInfo.getSize());
            }
        }
        Assert.assertEquals(2, fInfo.size());
    }

    public EntityInfo parallelFilesDir(){
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("parallelFiles/");
        fileInfo.setPath("/parallelFiles/");
        return fileInfo;
    }

    public EntityInfo anotherLayer(){
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("anotherLayer/");
        fileInfo.setPath("helloWorld/anotherLayer/");
        return fileInfo;
    }

    public EntityInfo helloWorldTwoFiles(){
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("helloWorld/");
        fileInfo.setPath("/helloWorld/");
        return fileInfo;
    }
}
