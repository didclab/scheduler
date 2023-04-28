package com.rabbitMq.rabbitmqscheduler.Services;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import org.bouncycastle.asn1.cms.MetaData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DropBoxExpander extends DestinationChunkSize implements FileExpander {

    private DbxClientV2 client;

    @Value("${dropbox.identifier}")
    private String odsClientID = "OneDataShare-DIDCLab";


    @Override
    public void createClient(EndpointCredential credential) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder(odsClientID).build();
        this.client = new DbxClientV2(config, ((EndpointCredential.getOAuthCredential(credential))).getToken());
    }

    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String parentPath, boolean overwrite) {
        Stack<Metadata> traversalQueue = new Stack<>();
        List<EntityInfo> expandedFiles = new ArrayList<>();
        if (parentPath == null || parentPath.isEmpty()) parentPath = "";
        //Expand all the files.
        if (userSelectedResources == null || userSelectedResources.isEmpty()) {
            List<Metadata> resources = listOp(parentPath);
            for (Metadata resource : resources) {
                if (resource instanceof FileMetadata) {
                    if (!overwrite && destinationFileExists(resource.getName(),parentPath)) {
                        continue; // Skip this file
                    }
                    expandedFiles.add(metaDataToFileInfo((FileMetadata) resource));
                } else if (resource instanceof FolderMetadata) {
                    traversalQueue.push(resource);
                }
            }
        } else {
            for (EntityInfo fileInfo : userSelectedResources) {
                List<Metadata> dropBoxFiles = listOp(fileInfo.getPath());
                dropBoxFiles.forEach(metadata -> {
                    if (metadata instanceof FileMetadata) {
                        if (!overwrite && destinationFileExists(metadata.getName(),fileInfo.getPath())) {
                            return; // Skip this file
                        }
                        expandedFiles.add(metaDataToFileInfo((FileMetadata) metadata));
                    } else if (metadata instanceof FolderMetadata) {
                        traversalQueue.push(metadata);
                    }
                });
            }
        }
        while (!traversalQueue.isEmpty()) {
            FolderMetadata folderMetadata = (FolderMetadata) traversalQueue.pop();
            List<Metadata> folderList = listOp(folderMetadata.getPathLower());
            for (Metadata res : folderList) {
                if (res instanceof FileMetadata) {
                    if (!overwrite && destinationFileExists(res.getName(),parentPath)) {
                        continue; // Skip this file
                    }
                    expandedFiles.add(metaDataToFileInfo((FileMetadata) res));
                } else if (res instanceof FolderMetadata) {
                    traversalQueue.push(res);
                }
            }
        }
        return expandedFiles;
    }
    private boolean destinationFileExists(String fileName, String destinationPath) {
        List<Metadata> destinationFiles = listOp(destinationPath);
        for (Metadata file : destinationFiles) {
            if (file.getName().equals(fileName) && file instanceof FileMetadata) {
                return true;
            }
        }
        return false;
    }

    public EntityInfo metaDataToFileInfo(FileMetadata file) {
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setSize(file.getSize());
        fileInfo.setId(file.getId());
        fileInfo.setPath(file.getPathLower());
        return fileInfo;
    }

    public List<Metadata> listOp(String path) {
        try {
            return this.client.files().listFolderBuilder(path).start().getEntries();
        } catch (DbxException e) {}
        try{
            return Collections.singletonList(this.client.files().getMetadata(path));
        } catch (DbxException e){}
        return new ArrayList<>();
    }

    @Override
    public List<EntityInfo> destinationChunkSize(List<EntityInfo> expandedFiles, String basePath, Integer userChunkSize){
        for(EntityInfo fileInfo : expandedFiles){
            if(fileInfo.getSize() < 8L << 20){
                fileInfo.setChunkSize(Long.valueOf(fileInfo.getSize()).intValue());
            }else if(userChunkSize < 4L << 20){
                fileInfo.setChunkSize(4000000);
            }else{
                fileInfo.setChunkSize(userChunkSize);
            }
        }
        return expandedFiles;
    }
}
