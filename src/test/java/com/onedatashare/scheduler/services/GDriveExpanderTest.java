package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.commonservice.model.credential.OAuthEndpointCredential;
import com.onedatashare.scheduler.services.expanders.GDriveExpander;
import junit.framework.TestCase;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class GDriveExpanderTest extends TestCase {

    GDriveExpander testObj;

    public OAuthEndpointCredential createCredential(){
        OAuthEndpointCredential credential = new OAuthEndpointCredential();
        credential.setToken(System.getenv("GDRIVE_JACOB_CREDENTIAL"));
        credential.setRefreshToken(System.getenv("GDRIVE_JACOB_REFRESH_CREDENTIAL"));
        return credential;
    }

    public void testExpandRoot(){
        testObj = new GDriveExpander();
        testObj.createClient(this.createCredential());
        List<EntityInfo> infoList = testObj.expandedFileSystem(new ArrayList<>(), "");
        Assert.isTrue(infoList.size() > 0, "The file info list turned up empty");
        for(EntityInfo fileInfo: infoList){
            Assert.isTrue(fileInfo != null, "The file info turned up null");
            Assert.isTrue(fileInfo.getSize() >= -1, "The file size was for some reason not greater than -1, -1 means its a google document or a similar mimeType and thus does not have a size");
            Assert.isTrue(fileInfo.getId() != null, "The file id turned up null");
            Assert.isTrue(!fileInfo.getId().isEmpty(), "The file id is empty");
            Assert.isTrue(fileInfo.getPath() != null, "the file path is null");
            Assert.isTrue(!fileInfo.getPath().isEmpty(), "the file path is empty");
            System.out.println(fileInfo.toString());
        }
    }

    public void testExpandTestFolder(){
        testObj = new GDriveExpander();
        testObj.createClient(this.createCredential());
        List<EntityInfo> infoList = testObj.expandedFileSystem(filesToExpand(), "");
        Assert.isTrue(infoList.size() > 0, "The file info list turned up empty");
        for(EntityInfo fileInfo: infoList){
            Assert.isTrue(fileInfo != null, "The file info turned up null");
            Assert.isTrue(fileInfo.getSize() >= -1, "The file size was for some reason not greater than -1, -1 means its a google document or a similar mimeType and thus does not have a size");
            Assert.isTrue(fileInfo.getId() != null, "The file id turned up null");
            Assert.isTrue(!fileInfo.getId().isEmpty(), "The file id is empty");
            Assert.isTrue(fileInfo.getPath() != null, "the file path is null");
            Assert.isTrue(!fileInfo.getPath().isEmpty(), "the file path is empty");
            System.out.println(fileInfo.toString());
        }

    }

    public List<EntityInfo> filesToExpand(){
        List<EntityInfo> fileInfo = new ArrayList<>();
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId("1vjx8gHofdgAA9gOTeh_LDWM_-Yw1xZWO");
        entityInfo.setPath("my-drive");
        fileInfo.add(entityInfo);
        return fileInfo;
    }

}