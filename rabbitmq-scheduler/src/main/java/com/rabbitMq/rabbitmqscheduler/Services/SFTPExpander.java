package com.rabbitMq.rabbitmqscheduler.Services;

import com.jcraft.jsch.*;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SFTPExpander implements FileExpander {

    AccountEndpointCredential credential;
    ChannelSftp channelSftp;
    List<EntityInfo> infoList;

    @SneakyThrows
    @Override
    public void createClient(EndpointCredential cred) {
        this.credential = EndpointCredential.getAccountCredential(cred);
        JSch jsch = new JSch();
        jsch.addIdentity("randomName", credential.getSecret().getBytes(), null, null);
        String[] destCredUri = credential.getUri().split(":");
        Session jschSession = jsch.getSession(credential.getUsername(), destCredUri[0], Integer.parseInt(destCredUri[1]));
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect();
        Channel sftp = jschSession.openChannel("sftp");
        ChannelSftp channelSftp = (ChannelSftp) sftp;
        channelSftp.connect();
        this.channelSftp = channelSftp;
    }

    @SneakyThrows
    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        this.infoList = userSelectedResources;
        List<EntityInfo> filesToTransferList = new LinkedList<>();
        Stack<ChannelSftp.LsEntry> traversalStack = new Stack<>();
        HashMap<String, String> fileNameToPathMap = new HashMap<>();
        if(userSelectedResources.isEmpty()){
            String path = credential.getUri() + basePath;
            Vector<ChannelSftp.LsEntry> fileVector = channelSftp.ls(path);
            for(ChannelSftp.LsEntry curr: fileVector){
                fileNameToPathMap.put(curr.getFilename(), path);
                traversalStack.add(curr);
            }
        }else{
            for (EntityInfo e : userSelectedResources) {
                String path = credential.getUri() + basePath + e.getId();
                Vector<ChannelSftp.LsEntry> fileVector = channelSftp.ls(path);
                for(ChannelSftp.LsEntry curr: fileVector){
                    fileNameToPathMap.put(curr.getFilename(), path);
                    traversalStack.add(curr);
                }
            }
        }
        for (int files = Integer.MAX_VALUE; files > 0 && !traversalStack.isEmpty(); --files) {
            ChannelSftp.LsEntry curr = traversalStack.pop();
            String parentPath = fileNameToPathMap.remove(curr.getFilename());
            if (curr.getAttrs().isDir()) {
                Vector<ChannelSftp.LsEntry> children = channelSftp.ls(parentPath + curr.getFilename());
                if(children.size() == 0){
                    EntityInfo fileInfo = new EntityInfo();
                    fileInfo.setId(curr.getFilename());
                    fileInfo.setSize(curr.getAttrs().getSize());
                    fileInfo.setPath(parentPath);
                }else{
                    for (ChannelSftp.LsEntry f : children) {
                        fileNameToPathMap.put(f.getFilename(), parentPath + curr.getFilename());
                        traversalStack.add(f);
                    }
                }
            } else if (!curr.getAttrs().isDir()) {
                EntityInfo fileInfo = new EntityInfo();
                fileInfo.setPath(parentPath);
                fileInfo.setId(curr.getFilename());
                fileInfo.setSize(curr.getAttrs().getSize());
                filesToTransferList.add(fileInfo);
            }
        }
        return filesToTransferList;
    }
}
