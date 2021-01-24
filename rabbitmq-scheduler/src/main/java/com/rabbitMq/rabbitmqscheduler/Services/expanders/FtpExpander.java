package com.rabbitMq.rabbitmqscheduler.Services.expanders;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FtpExpander {

    Logger logger = LoggerFactory.getLogger(FtpExpander.class);
    protected FileSystemManager fileSystemManager;

    public ArrayList<EntityInfo> expandFtpPath(TransferJobRequest.Source source) throws FileSystemException {
        StaticUserAuthenticator auth = new StaticUserAuthenticator(null, source.getVfsSourceCredential().getUsername(), source.getVfsSourceCredential().getSecret());
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(createFileSystemOptions(), auth);
        Stack<FileObject> traversalStack = new Stack<>();
        ArrayList<EntityInfo> filesToTransferList = new ArrayList<>();
        for (EntityInfo e : source.getInfoList()) {
            FileObject fObject = null;
            try {
                fObject = this.fileSystemManager.resolveFile(source.getVfsSourceCredential().getUri() + "/" + source.getParentInfo() + e.getPath(), createFileSystemOptions());
            } catch (FileSystemException fileSystemException) {
                logger.error("Experienced error reading from {}", source.getParentInfo().getPath());
                fileSystemException.printStackTrace();
            }
            traversalStack.push(fObject);
        }
        while (!traversalStack.isEmpty()) {
            FileObject curr = traversalStack.pop();
            if (curr.getType() == FileType.FOLDER) {
                traversalStack.addAll(Arrays.asList(curr.getChildren()));
                //Add empty folders as well
                if (curr.getChildren().length == 0) {
                    String filePath = curr.getPublicURIString().substring(source.getParentInfo().getPath().length());
                    EntityInfo fileInfo = new EntityInfo();
                    fileInfo.setPath(filePath);
                    filesToTransferList.add(fileInfo);
                }
            } else if (curr.getType() == FileType.FILE) {
                String filePath = curr.getPublicURIString().substring(source.getParentInfo().getPath().length());
                EntityInfo fileInfo = new EntityInfo();
                fileInfo.setPath(filePath);
                fileInfo.setSize(curr.getContent().getSize());
                filesToTransferList.add(fileInfo);
            }
        }
        return filesToTransferList;
    }

    public FileSystemOptions createFileSystemOptions() {
        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setFileType(opts, FtpFileType.BINARY);
        FtpFileSystemConfigBuilder.getInstance().setAutodetectUtf8(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setControlEncoding(opts, "UTF-8");
        return opts;
    }

}
