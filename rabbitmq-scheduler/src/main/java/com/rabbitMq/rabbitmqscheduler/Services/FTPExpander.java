package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import lombok.SneakyThrows;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FTPExpander implements FileExpander {

    AccountEndpointCredential vfsCredential;
    List<EntityInfo> infoList;


    public static FileSystemOptions generateOpts() {
        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setFileType(opts, FtpFileType.BINARY);
        FtpFileSystemConfigBuilder.getInstance().setAutodetectUtf8(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setControlEncoding(opts, "UTF-8");
        return opts;
    }

    @Override
    public void createClient(EndpointCredential credential) {
        this.vfsCredential = EndpointCredential.getAccountCredential(credential);
        StaticUserAuthenticator auth = new StaticUserAuthenticator(null, this.vfsCredential.getUsername(), this.vfsCredential.getSecret());
        try {
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(generateOpts(), auth);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        this.infoList = userSelectedResources;
        List<EntityInfo> filesToTransferList = new LinkedList<>();
        Stack<FileObject> traversalStack = new Stack<>();
        FileSystemManager fsm = VFS.getManager();
        if(infoList.isEmpty()){
            FileObject obj = fsm.resolveFile(this.vfsCredential.getUri() + basePath, generateOpts());
            traversalStack.push(obj);
        }else{
            for (EntityInfo e : this.infoList) {
                FileObject fObject = fsm.resolveFile(this.vfsCredential.getUri() + basePath + e.getId(), generateOpts());
                traversalStack.push(fObject);
            }
        }
        for (int files = Integer.MAX_VALUE; files > 0 && !traversalStack.isEmpty(); --files) {
            FileObject curr = traversalStack.pop();
            if (curr.getType() == FileType.FOLDER) {
                traversalStack.addAll(Arrays.asList(curr.getChildren()));
                //Add empty folders as well
                if (curr.getChildren().length == 0) {
                    String filePath = curr.getPublicURIString().substring(basePath.length());
                    EntityInfo fileInfo = new EntityInfo();
                    fileInfo.setId(curr.getName().getBaseName());
                    fileInfo.setPath(filePath);
                    filesToTransferList.add(fileInfo);
                }
            } else if (curr.getType() == FileType.FILE) {
                String filePath = curr.getPublicURIString().substring(basePath.length());
                EntityInfo fileInfo = new EntityInfo();
                fileInfo.setId(curr.getName().getBaseName());//this is the only part I am not sure of
                fileInfo.setPath(filePath);
                fileInfo.setSize(curr.getContent().getSize());
                filesToTransferList.add(fileInfo);
            }
        }
        return filesToTransferList;
    }
}
