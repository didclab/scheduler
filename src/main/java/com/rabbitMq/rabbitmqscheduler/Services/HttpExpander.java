package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.apache.tomcat.websocket.BasicAuthenticator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
public class HttpExpander extends DestinationChunkSize implements FileExpander {

    private AccountEndpointCredential credential;
    Logger logger = LoggerFactory.getLogger(HttpExpander.class);

    @Override
    public void createClient(EndpointCredential credential) {
        this.credential = EndpointCredential.getAccountCredential(credential);
        logger.info(this.credential.toString());
    }

    @SneakyThrows
    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath, boolean overwrite) {
        List<EntityInfo> filesToSend = new ArrayList<>();
        Stack<Element> directoriesToTraverse = new Stack<>();
        if (basePath.isEmpty()) basePath = "/";
        if (userSelectedResources.isEmpty()) { //we move the whole damn server
            logger.info(this.credential.getUri() + basePath);
            Document doc = Jsoup.connect(this.credential.getUri() + basePath).get();
            Elements links = doc.select("body a");
            for (Element elem : links) {
                if (elem.text().endsWith("/")) { //directory to expand
                    directoriesToTraverse.push(elem);
                } else { //we have a file
                    EntityInfo fileInfo = fromElement(elem);
                    boolean shouldTransfer = true;
                    if (!overwrite) {
                        for (EntityInfo existingFile : filesToSend) {
                            if (existingFile.getName().equals(fileInfo.getName()) &&
                                    existingFile.getPath().equals(fileInfo.getPath())) {
                                shouldTransfer = false;
                                break;
                            }
                        }
                    }
                    if (shouldTransfer) {
                        filesToSend.add(fileInfo);
                    }
                }
            }
        } else { //move only files/folders the user selected
            for (EntityInfo selectedFiles : userSelectedResources) {
                logger.info(this.credential.getUri() + basePath + selectedFiles.getPath());
                //we have a folder to transfer
                if(selectedFiles.getPath().endsWith("/")){
                    Document doc = Jsoup.connect(this.credential.getUri() + basePath + selectedFiles.getPath())
                            .ignoreContentType(true)
                            .get();
                    Elements links = doc.select("body a");
                    for (Element elem : links) {
                        if (elem.text().endsWith("/")) { //directory to expand
                            directoriesToTraverse.push(elem);
                        } else { //we have a file
                            EntityInfo entityInfo = fromElement(elem);
                            boolean shouldTransfer = true;
                            if (!overwrite) {
                                for (EntityInfo existingFile : filesToSend) {
                                    if (existingFile.getName().equals(entityInfo.getName()) &&
                                            existingFile.getPath().equals(entityInfo.getPath())) {
                                        shouldTransfer = false;
                                        break;
                                    }
                                }
                            }
                            if (shouldTransfer) {
                                filesToSend.add(entityInfo);
                            }
                        }
                    }
                }else{
                    EntityInfo entityInfo = this.fileToInfo(this.credential.getUri() + basePath + selectedFiles.getPath());
                    boolean shouldTransfer = true;
                    if (!overwrite) {
                        for (EntityInfo existingFile : filesToSend) {
                            if (existingFile.getName().equals(entityInfo.getName()) &&
                                    existingFile.getPath().equals(entityInfo.getPath())) {
                                shouldTransfer = false;
                                break;
                            }
                        }
                    }
                    if (shouldTransfer) {
                        filesToSend.add(entityInfo);
                    }
                }
            }
        }
        //all of these have names that should be appended to the path
        while (!directoriesToTraverse.isEmpty()) {
            Element directory = directoriesToTraverse.pop();
            if (directory.text().contains("..") || directory.text().contains(".")) {
                continue;
            }
            logger.info(directory.baseUri() + "/" + directory.text());
            Document doc = Jsoup.connect(directory.baseUri() + "/" + directory.text()).get();
            Elements links = doc.select("body a");
            for (Element elem : links) {
                if (elem.text().endsWith("/")) { //directory to expand
                    directoriesToTraverse.push(elem);
                } else { //we have a file
                    EntityInfo fileInfo = fromElement(elem);
                    boolean shouldTransfer = true;
                    if (!overwrite) {
                        for (EntityInfo existingFile : filesToSend) {
                            if (existingFile.getName().equals(fileInfo.getName()) &&
                                    existingFile.getPath().equals(fileInfo.getPath())) {
                                shouldTransfer = false;
                                break;
                            }
                        }
                    }
                    if (shouldTransfer) {
                        filesToSend.add(fileInfo);
                    }
                }
            }
        }
        return filesToSend;
    }

    public EntityInfo fromElement(Element elem) throws IOException {
        EntityInfo fileInfo = new EntityInfo();
        URL url = new URL(elem.absUrl("href"));
        long fileSize = url.openConnection().getContentLengthLong();
        logger.info("File Name:{}", elem.text());
        logger.info("file size={}", fileSize);
        logger.info("File Path: {}", url.getPath());
        fileInfo.setId(elem.text());
        fileInfo.setSize(fileSize);
        fileInfo.setPath(url.getPath());
        return fileInfo;
    }

    public EntityInfo fileToInfo(String strUrl) throws IOException {
        EntityInfo fileInfo = new EntityInfo();
        URL url = new URL(strUrl);
        URLConnection conn = url.openConnection();
        long fileSize = conn.getContentLengthLong();
        logger.info("File Name:{}", conn.getURL().getFile());
        logger.info("file size={}", fileSize);
        logger.info("File Path: {}", url.getPath());
        fileInfo.setId(conn.getURL().getFile());
        fileInfo.setSize(fileSize);
        fileInfo.setPath(url.getPath());
        return fileInfo;
    }
}
