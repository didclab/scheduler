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
        Session jschSession = null;
        JSch jsch = new JSch();
        String[] destCredUri = credential.getUri().split(":");
        boolean connected = false;
        try {
            jsch.addIdentity("randomName", credential.getSecret().getBytes(), null, null);
            jschSession = jsch.getSession(credential.getUsername(), destCredUri[0], Integer.parseInt(destCredUri[1]));
            jschSession.connect();
            jschSession.setConfig("StrictHostKeyChecking", "no");
            connected = true;
        } catch (JSchException ignored) {
            connected = false;
        }
        if (!connected) {
            try {
                jschSession = jsch.getSession(credential.getUsername(), destCredUri[0], Integer.parseInt(destCredUri[1]));
                jschSession.setConfig("StrictHostKeyChecking", "no");
                jschSession.setPassword(credential.getSecret());
                jschSession.connect();
                connected = true;
            } catch (JSchException ignored) {
                connected = false;
            }
        }
        if (!connected) {
            throw new JSchException("Unable to authenticate with the password/pem file");
        }
        ChannelSftp channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
        channelSftp.connect();
        this.channelSftp = channelSftp;
    }

    @SneakyThrows
    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        this.infoList = userSelectedResources;
        List<EntityInfo> filesToTransferList = new LinkedList<>();
        Stack<ChannelSftp.LsEntry> traversalStack = new Stack<>();
        HashMap<ChannelSftp.LsEntry, String> entryToFullPath = new HashMap<>();
        if (basePath.isEmpty() || basePath == null) {
            basePath = channelSftp.pwd() + "/";
        }
        if (userSelectedResources.isEmpty()) {
            Vector<ChannelSftp.LsEntry> fileVector = channelSftp.ls(basePath);
            for (ChannelSftp.LsEntry curr : fileVector) {
                entryToFullPath.put(curr, basePath + curr.getFilename());
                traversalStack.add(curr);
            }
        } else {
            for (EntityInfo e : userSelectedResources) {
                String path = basePath + e.getPath();
                Vector<ChannelSftp.LsEntry> fileVector = channelSftp.ls(path);
                for (ChannelSftp.LsEntry curr : fileVector) {
                    entryToFullPath.put(curr, path + curr.getFilename());
                    traversalStack.add(curr);
                }
            }
        }
        while (!traversalStack.isEmpty()) {
            ChannelSftp.LsEntry curr = traversalStack.pop();
            String fullPath = entryToFullPath.remove(curr);
            if (curr.getFilename().equals(".") || curr.getFilename().equals("..")) { //skip these two
                continue;
            }
            if (curr.getAttrs().isDir()) {
                Vector<ChannelSftp.LsEntry> children = channelSftp.ls(fullPath);
                if (children.size() == 0) {//this should include the empty directory
                    EntityInfo fileInfo = new EntityInfo();
                    fileInfo.setId(curr.getFilename());
                    fileInfo.setSize(curr.getAttrs().getSize());
                    fileInfo.setPath(fullPath);
                } else {
                    for (ChannelSftp.LsEntry f : children) {
                        entryToFullPath.put(f, fullPath + "/" + f.getFilename());
                        traversalStack.add(f);
                    }
                }
            } else if (!curr.getAttrs().isDir()) {
                EntityInfo fileInfo = new EntityInfo();
                fileInfo.setPath(fullPath);
                fileInfo.setId(curr.getFilename());
                fileInfo.setSize(curr.getAttrs().getSize());
                filesToTransferList.add(fileInfo);
            }
        }
        return filesToTransferList;
    }
}
