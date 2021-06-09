package com.rabbitMq.rabbitmqscheduler.Services;

import com.box.sdk.*;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Component
public class BoxExpander implements FileExpander{

    BoxAPIConnection connection;

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
        boolean isFile = false;
        for(EntityInfo selectedResource : userSelectedResources){
            try{
                BoxFile temp = new BoxFile(this.connection, selectedResource.getId());
                transferFiles.add(boxFileToEntityInfo(temp));
                isFile = true;
            }catch (BoxAPIException ignored){}
            try{
                if(!isFile){
                    BoxFolder temp = new BoxFolder(this.connection, selectedResource.getId());
                    travStack.push(temp);
                }
            }catch (BoxAPIException ignored){}
        }
        while(!travStack.isEmpty()){
            BoxFolder folder = travStack.pop();
            System.out.println(folder.getInfo().getName());
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

    public EntityInfo boxFileToEntityInfo(BoxFile boxFile) {
        BoxFile.Info boxFileInfo = boxFile.getInfo();
        EntityInfo fileInfo = new EntityInfo();
        fileInfo.setId(boxFileInfo.getID());
        fileInfo.setPath(boxFileInfo.getName());
        fileInfo.setSize(boxFileInfo.getSize());
        List<BoxFolder.Info> pathInfo = boxFileInfo.getPathCollection();
        String path = "";
        if(pathInfo == null) {
            path = "";
        }else{
            path = boxFileInfo.getPathCollection()
                    .stream().map(info -> {
                        String name = info.getName();
                        return name+"/";
                    }).collect(Collectors.joining());
        }
        fileInfo.setPath(path);
        return fileInfo;
    }

}
