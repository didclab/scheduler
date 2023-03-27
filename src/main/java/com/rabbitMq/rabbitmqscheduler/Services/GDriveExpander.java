package com.rabbitMq.rabbitmqscheduler.Services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.StartPageToken;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

@Service
public class GDriveExpander extends DestinationChunkSize implements FileExpander{

    Logger logger = LoggerFactory.getLogger(GDriveExpander.class);
    @Value("${gdrive.client.id}")
    private String gDriveClientId;

    @Value("${gdrive.client.secret}")
    private String gDriveClientSecret;

    @Value("${gdrive.appname}")
    private String gdriveAppName;

    private final String REQUEST_FILE_FIELDS = "id, name, size, mimeType, modifiedTime, md5Checksum, trashed, parents";
    private Drive client;

    @Override
    public void createClient(EndpointCredential credential) {
        OAuthEndpointCredential oauthCred = EndpointCredential.getOAuthCredential(credential);
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential1 = new GoogleCredential.Builder().setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(gDriveClientId, gDriveClientSecret)
                    .setTransport(transport).build();
            credential1.setAccessToken(oauthCred.getToken());
            credential1.setRefreshToken(oauthCred.getRefreshToken());
            this.client = new Drive.Builder(transport, GsonFactory.getDefaultInstance(), credential1)
                    .setApplicationName(gdriveAppName)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        Stack<File> fileListStack = new Stack<>();
        List<EntityInfo> fileInfoList = new ArrayList<>();
            for(EntityInfo fileInfo : userSelectedResources){
                String fileQuery = "'" + fileInfo.getId() + "' in parents and trashed=false";
                EntityInfo info = getMetadataForfile(fileInfo.getId());
                if(info==null){
                    fileInfoList.addAll(googleDriveLister(fileListStack, fileQuery, fileInfo.getId()));
                } else{
                    fileInfoList.add(info);
                }
        }

        while(!fileListStack.isEmpty()){
            File file = fileListStack.pop();
            String fileQuery = "'" + file.getId() + "' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed=false";
            googleDriveLister(fileListStack, fileQuery, file.getId());
        }
        return fileInfoList;
    }

    private List<EntityInfo> googleDriveLister(Stack<File> fileListStack, String fileQuery, String folderId) {
        FileList fileList;
        String pageToken = "";

        List<EntityInfo> folderFileList = new ArrayList<>();
        try {
            do {
                fileList = this.client.files().list()
                        .setQ(fileQuery)
                        .setFields("nextPageToken, files(id, name, parents, size, mimeType)")
                        .setPageToken(pageToken)
                        .execute();

                for(File file : fileList.getFiles()){
                    if(file.getId().equals(folderId)){//the folder that u query appears in the result
                        continue;
                    }
                    if(file.getMimeType().equals("application/vnd.google-apps.folder")){
                        fileListStack.add(file);
                    }else{
                        folderFileList.add(getMetadataForfile(file.getId()));
                    }
                }
                pageToken = fileList.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            logger.error("Error listing files from Google drive",e);
        }
        return folderFileList;
    }

    public EntityInfo getMetadataForfile(String fileId){
        try {
            File file = this.client.files().get(fileId)
                    .setFields(REQUEST_FILE_FIELDS)
                    .execute();
            return googleFileToEntityInfo(file);
        } catch(IOException e){
            e.printStackTrace();
        }
       return null;
    }

    private EntityInfo googleFileToEntityInfo(File googleFile){
        if(googleFile.getId() == null || googleFile.getParents() == null || googleFile.getSize() == null || googleFile.getMd5Checksum() == null){
            return null;
        }
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setId(googleFile.getId());
        entityInfo.setSize(googleFile.getSize());
        entityInfo.setPath(String.valueOf(googleFile.getParents()));
        entityInfo.setChecksum(googleFile.getMd5Checksum());
        return entityInfo;
    }
}
