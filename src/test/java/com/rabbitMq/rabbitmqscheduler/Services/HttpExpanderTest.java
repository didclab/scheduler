package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class HttpExpanderTest extends TestCase {

    HttpExpander testObj;

    public AccountEndpointCredential createApacheCCCred(){
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setAccountId("ccApache2");
        accountEndpointCredential.setUri("http5://192.5.87.31:80/");
        accountEndpointCredential.setUsername("");
        accountEndpointCredential.setSecret("");
        return accountEndpointCredential;
    }

    public void testClientInit(){
        testObj = new HttpExpander();
        testObj.createClient(this.createApacheCCCred());
        Assert.assertNotNull("Credential is null", testObj.vfsCredential);
        Assert.assertNotNull("VfsManager is null", testObj.vfsManager);
    }

    public void testEmptyFileList(){
        testObj = new HttpExpander();
        testObj.createClient(this.createApacheCCCred());
        List<EntityInfo> files = testObj.expandedFileSystem(new ArrayList<>(), "/");
        for(EntityInfo fileInfo : files){
            System.out.println(fileInfo.toString());
        }
        Assert.assertEquals(4,files.size());
    }

    public void testListTestDirApache(){
        testObj = new HttpExpander();
        testObj.createClient(this.createApacheCCCred());
        ArrayList<EntityInfo> dir = new ArrayList<>();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("testDir/");
        fileInfo.setPath("/testDir/");
        dir.add(fileInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(dir, "/");
        for(EntityInfo f : files){
            System.out.println(f.toString());
        }
        Assert.assertEquals(4,files.size());

    }

    public void testMontyDmgOneFile(){
        testObj = new HttpExpander();
        testObj.createClient(this.createApacheCCCred());
        List<EntityInfo> filesToExpand = new ArrayList<>();
        EntityInfo fInfo = new EntityInfo();
        fInfo.setId("monty-1.dmg");
        fInfo.setPath("/monty-1.dmg");
        filesToExpand.add(fInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(filesToExpand, "/");
        for(EntityInfo fileInfo : files){
            System.out.println(fileInfo.toString());
        }
        Assert.assertEquals(1,files.size());
        Assert.assertEquals("monty-1.dmg",files.get(0).getId());
        Assert.assertEquals(1073741824,files.get(0).getSize());
    }

    public void testTwoMontyDmgFiles(){
        testObj = new HttpExpander();
        testObj.createClient(this.createApacheCCCred());
        List<EntityInfo> filesToExpand = new ArrayList<>();
        EntityInfo fInfo = new EntityInfo();
        fInfo.setId("monty-1.dmg");
        fInfo.setPath("/monty-1.dmg");
        filesToExpand.add(fInfo);
        EntityInfo monty2 = new EntityInfo();
        monty2.setId("monty-2.dmg");
        monty2.setPath("/monty-2.dmg");
        filesToExpand.add(monty2);
        List<EntityInfo> files = testObj.expandedFileSystem(filesToExpand, "/");
        for(EntityInfo fileInfo : files){
            System.out.println(fileInfo.toString());
            Assert.assertEquals(1073741824, fileInfo.getSize());
        }
        Assert.assertEquals(2,files.size());
    }
}
