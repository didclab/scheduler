package com.rabbitMq.rabbitmqscheduler.Services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class SFTPExpanderTest extends TestCase {

    SFTPExpander testObj;

    public String readInPemFile() throws IOException {
        String path = "/Users/jacobgoldverg/.ssh/endpoint-cred-dev.pem";// this is something that will need to change to a path that is locally available
        File file = new File(path);
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
        String publicKeyPEM = key;
        return publicKeyPEM;
    }

    public AccountEndpointCredential rebexCredentialTest(){
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setUri("sftp://test.rebex.net:22");
        accountEndpointCredential.setUsername("demo");
        accountEndpointCredential.setSecret("password");
        accountEndpointCredential.setAccountId("test");
        return accountEndpointCredential;
    }

    public AccountEndpointCredential createTestCredential() {
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

    public void testCreateClientIsNotNull() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        Assert.isTrue(testObj.channelSftp.isConnected(), "The channel is not connected to the credentials given");
        Assert.isTrue(testObj.channelSftp != null);
    }

    public void testCreateClientIsListing() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        try {
            Vector<ChannelSftp.LsEntry> files = testObj.channelSftp.ls("/");
            Assert.notNull(files);
            Assert.isTrue(files.size() > 0, "the list of files is not greater than 0");
        } catch (SftpException sftpException) {
            sftpException.printStackTrace();
        }
    }

    public List<EntityInfo> listTestDir() {
        ArrayList<EntityInfo> list = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("endpoint-cred-service/src/main/java/org/onedatashare/endpointcredentials/service/");
        entityInfo.setPath("endpoint-cred-service/src/main/java/org/onedatashare/endpointcredentials/service/");
        list.add(entityInfo);
        return list;
    }

    public EntityInfo listTwoFiles(){
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("endpoint-cred-service/src/main/java/org/onedatashare/endpointcredentials/service/");
        entityInfo.setPath("endpoint-cred-service/src/main/java/org/onedatashare/endpointcredentials/service/");
        return entityInfo;
    }
    public EntityInfo listOneFile(){
        EntityInfo info = new EntityInfo();
        info.setId("endpoint-cred-service/src/main/resources/");
        info.setPath("endpoint-cred-service/src/main/resources/");
        return info;
    }

    public ArrayList<EntityInfo> listTwoDirectories(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        list.add(listTwoFiles());
        list.add(listOneFile());
        return list;
    }

    public EntityInfo destDir(){
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setPath("Dest/");
        fileInfo.setId("Dest/");
        return fileInfo;
    }

    public ArrayList<EntityInfo> listThreeDirAndFile(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        list.add(listTwoFiles());
        list.add(listOneFile());
//        list.add(destDir());
        list.add(file());
        return list;
    }

    public EntityInfo file(){
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId("rds-combined-ca-bundle.pem");
        fileInfo.setPath("rds-combined-ca-bundle.pem");
        return fileInfo;
    }

    public List<EntityInfo> oneFile(){
        ArrayList<EntityInfo> list = new ArrayList<>();
        list.add(file());
        return list;
    }

    public void testExpandedFileSystem() {
        testObj = new SFTPExpander();
        testObj.createClient(rebexCredentialTest());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(new ArrayList<>(), "");
        expandedList.forEach(entityInfo -> {
            System.out.println(entityInfo.toString());
        });
        Assert.isTrue(expandedList.size() > 0, "The client was not listing home");
    }

    public void testTheUserHome() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(new ArrayList<>(), "fulltest/");
        Assert.isTrue(expandedList.size() > 0, "unable to list the users home");
        for (EntityInfo fileInfo : expandedList) {
            System.out.println(fileInfo.toString());
            Assert.isTrue(fileInfo.getPath().length() >= fileInfo.getId().length(), "The file path is shorter than the file id");
        }
    }

    public void testExpandFileSystemTwoDifferentDirectories() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(listTwoDirectories(), "/home/ubuntu/");
        Assert.isTrue(expandedList.size() == 3, "The size should be 3 ");
        for (EntityInfo info : expandedList) {
            System.out.println(info.toString());
            if (info.getId().equals(".") || info.getId().equals("..")) {
                Assert.doesNotContain(info.getId(), ".", "has a dot in the path which is wrong");
                Assert.doesNotContain(info.getId(), "..", "has a two dots in the path which is wrong");
            }
        }
    }

    public void testOneFile(){
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(oneFile(), "");
        for (EntityInfo info : expandedList) {
            System.out.println(info.toString());
            if (info.getId().equals(".") || info.getId().equals("..")) {
                Assert.doesNotContain(info.getId(), ".", "has a dot in the path which is wrong");
                Assert.doesNotContain(info.getId(), "..", "has a two dots in the path which is wrong");
            }
        }

    }

    public void testExpandedFileSystemThreeLayer() {
        testObj = new SFTPExpander();
        testObj.createClient(createTestCredential());
        List<EntityInfo> expandedList = testObj.expandedFileSystem(listThreeDirAndFile(), "");
        for (EntityInfo info : expandedList) {
            System.out.println(info.toString());
            if (info.getId().equals(".") || info.getId().equals("..")) {
                Assert.doesNotContain(info.getId(), ".", "has a dot in the path which is wrong");
                Assert.doesNotContain(info.getId(), "..", "has a two dots in the path which is wrong");
            }
        }
    }


}