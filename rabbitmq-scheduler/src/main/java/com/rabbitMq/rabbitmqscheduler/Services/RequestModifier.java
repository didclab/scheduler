package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RequestModifier {
    private static final Logger logger = LoggerFactory.getLogger(RequestModifier.class);

    @Autowired
    CredentialService credentialService;

    @Autowired
    SFTPExpander sftpExpander;
    @Autowired
    FTPExpander ftpExpander;
    @Autowired
    S3Expander s3Expander;

    Set<String> nonOautUsingType = new HashSet<>(Arrays.asList(new String[]{"ftp", "sftp", "http", "vfs", "s3"}));

    public List<EntityInfo> selectAndExpand(EndPointType type, EndpointCredential credential, List<EntityInfo> selectedResources, String basePath){
        switch (type){
            case ftp:
                ftpExpander.createClient(credential);
                return ftpExpander.expandedFileSystem(selectedResources, basePath);
            case s3:
                s3Expander.createClient(credential);
                return s3Expander.expandedFileSystem(selectedResources, basePath);
            case sftp:
                sftpExpander.createClient(credential);
                return sftpExpander.expandedFileSystem(selectedResources, basePath);
            case box:
                return null;
            case gftp:
                return null;
            case http:
                return null;
            case dropbox:
                return null;
            case gdrive:
                return null;
            case vfs:
                return null;
        }
        return null;
    }

    public TransferJobRequest createRequest(RequestFromODS odsTransferRequest) {
        logger.info("hit createRequest ");
        TransferJobRequest transferJobRequest = new TransferJobRequest();
        transferJobRequest.setJobId(odsTransferRequest.getId());
        transferJobRequest.setChunkSize(odsTransferRequest.getChunkSize());
        transferJobRequest.setOptions(odsTransferRequest.getOptions());
        transferJobRequest.setOwnerId(odsTransferRequest.getUserId());
        transferJobRequest.setPriority(1);
        TransferJobRequest.Source s = new TransferJobRequest.Source();
        s.setInfoList(odsTransferRequest.getSource().getInfoList());
        s.setParentInfo(odsTransferRequest.getSource().getParentInfo());
        s.setType(odsTransferRequest.getSource().getType());
        TransferJobRequest.Destination d = new TransferJobRequest.Destination();
        d.setParentInfo(odsTransferRequest.getDestination().getParentInfo());
        d.setType(odsTransferRequest.getDestination().getType());
        EndpointCredential sourceCredential;
        EndpointCredential destinationCredential;
        if (nonOautUsingType.contains(odsTransferRequest.getSource().getType().toString())) {
            sourceCredential = credentialService.fetchAccountCredential(odsTransferRequest.getSource().getType(), odsTransferRequest.getUserId(), odsTransferRequest.getSource().getAccountId());
            s.setVfsSourceCredential(EndpointCredential.getAccountCredential(sourceCredential));
            logger.info(sourceCredential.toString());
        } else {
            sourceCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getSource().getType(), odsTransferRequest.getUserId(), odsTransferRequest.getSource().getAccountId());
            s.setOauthSourceCredential(EndpointCredential.getOAuthCredential(sourceCredential));
            logger.info(sourceCredential.toString());
        }
        if (nonOautUsingType.contains(odsTransferRequest.getDestination().getType().toString())) {
            destinationCredential = credentialService.fetchAccountCredential(odsTransferRequest.getDestination().getType(), odsTransferRequest.getUserId(), odsTransferRequest.getSource().getAccountId());
            d.setVfsDestCredential(EndpointCredential.getAccountCredential(destinationCredential));
            logger.info(destinationCredential.toString());
        } else {
            destinationCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getDestination().getType(), odsTransferRequest.getUserId(), odsTransferRequest.getSource().getAccountId());
            d.setOauthDestCredential(EndpointCredential.getOAuthCredential(destinationCredential));
            logger.info(destinationCredential.toString());
        }
        List<EntityInfo> expandedFiles = selectAndExpand(s.getType(), sourceCredential, odsTransferRequest.getSource().getInfoList(),odsTransferRequest.getSource().getParentInfo().getPath());
        s.setInfoList(expandedFiles);
        transferJobRequest.setSource(s);
        transferJobRequest.setDestination(d);
        return transferJobRequest;
    }

//    private OAuthEndpointCredential getOautCred(String userId, String accountId, EndPointType type) {
//        String urlToRead = credBaseUri + userId + "/" + type + "/" + accountId;
//        OAuthEndpointCredential oAuthEndpointCredential = null;
//        String jsonString = getResponseFromCred(urlToRead);
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            oAuthEndpointCredential = objectMapper.readValue(jsonString, OAuthEndpointCredential.class);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        return oAuthEndpointCredential;
//    }
//
//    private AccountEndpointCredential getNonOautCred(String userId, String accountId, EndPointType type) {
//        String urlToRead = credBaseUri + userId + "/" + type + "/" + accountId;
//        AccountEndpointCredential accountEndpointCredential = null;
//        String jsongString = getResponseFromCred(urlToRead);
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            accountEndpointCredential = objectMapper.readValue(jsongString, AccountEndpointCredential.class);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        return accountEndpointCredential;
//    }
//
//    private String getResponseFromCred(String urlToRead) {
//        logger.info("Hitting cred service with url : " + urlToRead);
//        StringBuilder line = new StringBuilder();
//        try {
//            URL url = new URL(urlToRead);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.connect();
//            if (conn.getResponseCode() != 200) {
//                logger.error("Not able to retrive nonOauth cred");
//                throw new RuntimeException("HttpResponseCode : " + conn.getResponseCode());
//            } else {
//                Scanner sc = new Scanner(url.openStream());
//                while (sc.hasNext()) {
//                    line.append(sc.nextLine());
//                }
//                sc.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return line.toString();
//    }
}
