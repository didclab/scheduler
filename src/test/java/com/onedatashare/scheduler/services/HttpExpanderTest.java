package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.services.expanders.HttpExpander;
import com.onedatashare.commonservice.model.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class HttpExpanderTest extends TestCase {
    HttpExpander testObj;

    public AccountEndpointCredential credential(){
        AccountEndpointCredential cred = new AccountEndpointCredential();
        cred.setAccountId("testHttpServer");
        cred.setUsername("cc");
        cred.setUri("http://129.114.108.180:80");
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
    public void testParallelFile(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> directoryToExpand = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("parallel_file.txt");
        entityInfo.setPath("/parallel_file.txt");
        directoryToExpand.add(entityInfo);

        List<EntityInfo> files = testObj.expandedFileSystem(directoryToExpand, "/");
        Assert.assertTrue(files.size() == 1);
        Assert.assertTrue(files.get(0).getId().equals("parallel_file.txt"));
        Assert.assertEquals(10737418240L, files.get(0).getSize());
    }
    public void testParallelDirectory(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> directoryToExpand = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("parallel/");
        entityInfo.setPath("/parallel/");
        directoryToExpand.add(entityInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(directoryToExpand, "/");
        for(EntityInfo file : files){
            Assert.assertTrue(file.getId().contains("parallel_file.txt"));
            Assert.assertEquals(10737418240L, file.getSize());
        }
        Assert.assertEquals(4, files.size());
    }

    public void testConcurrencyDir(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> directoryToExpand = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("concurrency/");
        entityInfo.setPath("/concurrency/");
        directoryToExpand.add(entityInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(directoryToExpand, "/");
        for(EntityInfo file : files){
            Assert.assertTrue(file.getId().contains("conc_file.txt"));
            Assert.assertEquals(524288000, file.getSize());
        }
        Assert.assertEquals(40, files.size());

    }
    public void testCCDirAndPDir(){
        testObj = new HttpExpander();
        testObj.createClient(this.credential());
        ArrayList<EntityInfo> directoryToExpand = new ArrayList<>();
        EntityInfo pInfo = new EntityInfo();
        pInfo.setId("parallel/");
        pInfo.setPath("/parallel/");

        EntityInfo ccInfo = new EntityInfo();
        ccInfo.setId("concurrency/");
        ccInfo.setPath("/concurrency/");

        directoryToExpand.add(pInfo);
        directoryToExpand.add(ccInfo);
        List<EntityInfo> files = testObj.expandedFileSystem(directoryToExpand, "");
        for(EntityInfo fileInfo : files){
            Assert.assertTrue(fileInfo.getId().contains("parallel_file.txt") || fileInfo.getId().contains("conc_file.txt"));
            Assert.assertTrue(fileInfo.getSize() == 10737418240L || fileInfo.getSize() == 1073741824L);
        }
        Assert.assertEquals(85, files.size());
    }
}
