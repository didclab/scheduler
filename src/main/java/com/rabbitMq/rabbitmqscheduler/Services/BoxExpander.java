package com.rabbitMq.rabbitmqscheduler.Services;

import com.box.sdk.*;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Component
public class BoxExpander extends DestinationChunkSize implements FileExpander {

    BoxAPIConnection connection;
    Logger logger = LoggerFactory.getLogger(BoxExpander.class);

    @Override
    public void createClient(EndpointCredential credential) {
        OAuthEndpointCredential oAuthEndpointCredential = EndpointCredential.getOAuthCredential(credential);
        connection = new BoxAPIConnection(oAuthEndpointCredential.getToken());
    }

    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        List<EntityInfo> transferFiles = new ArrayList<>();
        Stack<BoxFolder> travStack = new Stack<>();//this will only hold folders to traverse
        if(userSelectedResources.isEmpty()) return new ArrayList<>(); //we need to signal the cancellation of this transferjob request.
        for(EntityInfo selectedResource : userSelectedResources){
            boolean isFile = false;
            try{
                BoxFile temp = new BoxFile(this.connection, selectedResource.getId());
                transferFiles.add(boxFileToEntityInfo(temp));
                isFile = true;
            }catch (BoxAPIException ignored){
                logger.info("Tried to open {} as a file but it did not work", selectedResource.toString());
                isFile = false;
            }
            if(!isFile){
                try{
                    BoxFolder temp = new BoxFolder(this.connection, selectedResource.getId());
                    travStack.push(temp);
                }catch (BoxAPIException ignored){
                    logger.info("Tried to open {} as a folder but it did not work", selectedResource.toString());
                }
            }
        }
        while(!travStack.isEmpty()){
            BoxFolder folder = travStack.pop();
            for(BoxItem.Info child : folder){
                if (child instanceof BoxFile.Info) {
                    BoxFile.Info fileInfo = (BoxFile.Info) child;
                    BoxFile boxFile = new BoxFile(this.connection, fileInfo.getID());
                    transferFiles.add(boxFileToEntityInfo(boxFile));
                } else if (child instanceof BoxFolder.Info) {
                    BoxFolder.Info folderInfo = (BoxFolder.Info) child;
                    BoxFolder childFolder = new BoxFolder(this.connection, folderInfo.getID());
                    travStack.push(childFolder);
                }
            }
        }
        return transferFiles;
    }

    @Override
    public List<EntityInfo> destinationChunkSize(List<EntityInfo> expandedFiles, String basePath, Integer userChunkSize) {
        BoxFolder destinationUploadFolder = new BoxFolder(this.connection, basePath);
        for(EntityInfo entityInfo : expandedFiles){
            if(entityInfo.getSize() < 1024*1024*20){
                entityInfo.setChunkSize(Math.toIntExact(entityInfo.getSize()));
            }else{
                BoxFileUploadSession.Info uploadSession = destinationUploadFolder.createUploadSession(entityInfo.getId(), entityInfo.getSize());
                entityInfo.setChunkSize(uploadSession.getPartSize());
            }
        }
        return expandedFiles;
    }


    public EntityInfo boxFileToEntityInfo(BoxFile boxFile) {
        BoxFile.Info boxFileInfo = boxFile.getInfo();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId(boxFileInfo.getID());
        fileInfo.setPath(boxFileInfo.getParent().getID());
        fileInfo.setSize(boxFileInfo.getSize());
        return fileInfo;
    }
}
