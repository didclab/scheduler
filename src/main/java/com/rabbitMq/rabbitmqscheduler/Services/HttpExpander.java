package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import lombok.SneakyThrows;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.provider.http5.Http5FileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

@Component
public class HttpExpander extends DestinationChunkSize implements FileExpander{

    AccountEndpointCredential vfsCredential;
    List<EntityInfo> infoList;
    Logger logger;
    FileSystemOptions options;
    FileSystemManager vfsManager;

    public HttpExpander(){
        this.options = new FileSystemOptions();
        this.logger = LoggerFactory.getLogger(HttpExpander.class);
    }

    @SneakyThrows
    @Override
    public void createClient(EndpointCredential credential) {
        this.vfsCredential = EndpointCredential.getAccountCredential(credential);
        StaticUserAuthenticator auth = new StaticUserAuthenticator(null, this.vfsCredential.getUsername(), this.vfsCredential.getSecret());
        Http5FileSystemConfigBuilder builder = Http5FileSystemConfigBuilder.getInstance();
//        builder.setRootURI(this.options, "/");
        builder.setFollowRedirect(this.options, true);
        builder.setRootURI(this.options, "/");
        this.vfsManager = VFS.getManager();
    }

    @SneakyThrows
    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        this.infoList = userSelectedResources;
        List<EntityInfo> filesToTransferList = new LinkedList<>();
        Stack<FileObject> traversalStack = new Stack<>();
        if(basePath.isEmpty() || basePath == null || !basePath.endsWith("/")) basePath += "/";
        if(infoList.isEmpty()){
            FileObject obj = this.vfsManager.resolveFile(this.vfsCredential.getUri() + basePath, this.options);
            logger.info(String.valueOf(obj.getType()));
            logger.info(obj.getName().getBaseName());
            Arrays.stream(obj.getChildren()).forEach(fileObject -> traversalStack.push(fileObject));
        }else{
            for (EntityInfo e : this.infoList) {
                FileObject fObject = this.vfsManager.resolveFile(this.vfsCredential.getUri() + basePath + e.getId(), this.options);
                traversalStack.push(fObject);
            }
        }
        for (int files = Integer.MAX_VALUE; files > 0 && !traversalStack.isEmpty(); --files) {
            FileObject curr = traversalStack.pop();
            FileName fileName = curr.getName();
            URI uri = URI.create(fileName.getURI());
            logger.info(uri.toString());
            curr.refresh();
            if (curr.getType().equals(FileType.FOLDER)) {
                traversalStack.addAll(Arrays.asList(curr.getChildren()));
                //Add empty folders as well
                if (curr.getChildren().length == 0) {
                    EntityInfo fileInfo = new EntityInfo();
                    fileInfo.setId(fileName.getBaseName());
                    fileInfo.setPath(uri.getPath());
                    filesToTransferList.add(fileInfo);
                }
            } else if (curr.getType().equals(FileType.FILE)) {
                EntityInfo fileInfo = new EntityInfo();
                fileInfo.setId(curr.getName().getBaseName());
                fileInfo.setPath(uri.getPath());
                fileInfo.setSize(curr.getContent().getSize());
                filesToTransferList.add(fileInfo);
            }
        }
        return filesToTransferList;
    }
}
