package com.rabbitMq.rabbitmqscheduler.Services;

import com.jcraft.jsch.SftpException;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class SFTPExpanderTest extends TestCase {


    SFTPExpander testObj;
    public String readInPemFile() throws IOException {
        String path = "~/.ssh/SFTP-US-West.pem";
        File file = new File(path);
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");
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
            Assert.isTrue(testObj.channelSftp.ls("") != null);
        }catch (SftpException sftpException){
            sftpException.printStackTrace();
        }
    }

    public void testExpandedFileSystem() {
    }
}