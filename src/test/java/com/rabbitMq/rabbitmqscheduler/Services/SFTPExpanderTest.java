package com.rabbitMq.rabbitmqscheduler.Services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import javax.validation.constraints.AssertFalse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class SFTPExpanderTest extends TestCase {

    SFTPExpander testObj;

    public String readInPemFile() throws IOException {
        String path = "/Users/jacobgoldverg/testKeys/SFTP-US-West.pem";// this is something that will need to change to a path that is locally available
        File file = new File(path);
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
        String publicKeyPEM = key;
        return publicKeyPEM;
    }

    public AccountEndpointCredential createTestCredential(){
        String SFTPTESTSERVER = System.getenv("SFTP_TEST_SERVER");
        String SFTPTESTSERVERUSER = System.getenv("SFTP_TEST_SERVER_USER");
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        try {
            accountEndpointCredential.setSecret(readInPemFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        accountEndpointCredential.setAccountId("hello@test.com");
        accountEndpointCredential.setUri(SFTPTESTSERVER);
        accountEndpointCredential.setUsername(SFTPTESTSERVERUSER);
        return accountEndpointCredential;
    }

    public void testCreateClientIsNotNull(){
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        Assert.isTrue(testObj.channelSftp != null);
    }

    public void testCreateClientIsListing(){
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        try{
            Vector<ChannelSftp.LsEntry> files = testObj.channelSftp.ls(".");
            Assert.notNull(files);
            Assert.isTrue(files.size() > 0);
        }catch (SftpException sftpException){
            sftpException.printStackTrace();
        }
    }

    public List<EntityInfo> listPipeDataSet(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("");
        entityInfo.setPath("pipeDataSet/");
        list.add(entityInfo);
        return list;
    }
    public List<EntityInfo> listFtest(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("");
        entityInfo.setPath("ftest/");
        list.add(entityInfo);
        return list;
    }

    public List<EntityInfo> listTestDir(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("");
        entityInfo.setPath("test/");
        list.add(entityInfo);
        return list;
    }

    public void testExpandedFileSystem() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        try{
            Vector<ChannelSftp.LsEntry> files = testObj.channelSftp.ls(".");
            List<EntityInfo> expandedList = testObj.expandedFileSystem(new ArrayList<>(), "");
        }catch (SftpException sftpException){
            sftpException.printStackTrace();
        }
    }
    public void testExpandedFileSystemWithOneDirectory() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        try{
            Vector<ChannelSftp.LsEntry> files = testObj.channelSftp.ls("/home/ubuntu/pipeDataSet");
            List<EntityInfo> expandedList = testObj.expandedFileSystem(listPipeDataSet(), "/home/ubuntu/");
            int totalInFiles = files.size() -2;//the reason for this is we will get back "." and "..";
            Assert.isTrue(expandedList.size() == totalInFiles, "The number of listed files is greater or less than the files in pipeDataSet on SFTP US West");
        }catch (SftpException sftpException){
            sftpException.printStackTrace();
        }
    }

    public void testExpandedFileSystemWithNestedDirectories() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(listFtest(), "/home/ubuntu/");
        Assert.isTrue(expandedList.size() == 125, "The size should be 125 ");
        for(EntityInfo info: expandedList){
            if(info.getId().equals(".") || info.getId().equals("..")){
                Assert.doesNotContain(info.getId(), ".", "has a dot in the path which is wrong");
                Assert.doesNotContain(info.getId(), "..", "has a two dots in the path which is wrong");
            }
        }
    }

    public void testExpandedFileSystemThreeLayer() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(listTestDir(), "/home/ubuntu/");
        Assert.isTrue(expandedList.size() == 3, "The size should be 3 ");
    }


}