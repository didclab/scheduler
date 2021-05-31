package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferOptions;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
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

//    @Value("${cred.service.uri}")
//    String credBaseUri;

    @Autowired
    SFTPExpander sftpExpander;
    @Autowired
    FTPExpander ftpExpander;
    @Autowired
    S3Expander s3Expander;

    Set<String> nonOautUsingType = new HashSet<>(Arrays.asList(new String[]{"ftp", "sftp", "http", "vfs", "s3"}));
//    Set<String> oautUsingType = new HashSet<>(Arrays.asList(new String[]{ "dropbox", "box", "gdrive", "gftp"}));

    public List<EntityInfo> selectAndExpand(TransferJobRequest.Source source, List<EntityInfo> selectedResources){
        switch (source.getType()){
            case ftp:
                ftpExpander.createClient(source.getVfsSourceCredential());
                logger.info("Expanding FTP");
                logger.info(selectedResources.toString());
                logger.info(source.getParentInfo().getPath());
                return ftpExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case s3:
                logger.info("Expanding S3");
                s3Expander.createClient(source.getVfsSourceCredential());
                return s3Expander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case sftp:
                logger.info("Expanding SFTP");
                sftpExpander.createClient(source.getVfsSourceCredential());
                return sftpExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
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
        logger.info(odsTransferRequest.toString());
        TransferJobRequest transferJobRequest = new TransferJobRequest();
        transferJobRequest.setJobId(odsTransferRequest.getOwnerId());
        transferJobRequest.setOptions(TransferOptions.createTransferOptionsFromUser(odsTransferRequest.getOptions()));
        transferJobRequest.setOwnerId(odsTransferRequest.getOwnerId());
        transferJobRequest.setPriority(1);//need some way of creating priority depending on factors. Memberyship type? Urgency of transfer, prob need create these groups
        TransferJobRequest.Source s = new TransferJobRequest.Source();
        s.setInfoList(odsTransferRequest.getSource().getInfoList());
        logger.info(odsTransferRequest.getSource().getParentInfo().toString());
        s.setParentInfo(odsTransferRequest.getSource().getParentInfo());
        s.setType(odsTransferRequest.getSource().getType());
        TransferJobRequest.Destination d = new TransferJobRequest.Destination();
        d.setParentInfo(odsTransferRequest.getDestination().getParentInfo());
        d.setType(odsTransferRequest.getDestination().getType());
        if (nonOautUsingType.contains(odsTransferRequest.getSource().getType().toString())) {
            AccountEndpointCredential sourceCredential =credentialService.fetchAccountCredential(odsTransferRequest.getSource().getType().toString(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            logger.info(sourceCredential.toString());
            s.setVfsSourceCredential(sourceCredential);
        } else {
            OAuthEndpointCredential sourceCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getSource().getType(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            s.setOauthSourceCredential(sourceCredential);
        }
        if (nonOautUsingType.contains(odsTransferRequest.getDestination().getType().toString())) {
            AccountEndpointCredential destinationCredential =  credentialService.fetchAccountCredential(odsTransferRequest.getDestination().getType().toString(), odsTransferRequest.getOwnerId(), odsTransferRequest.getDestination().getCredId());
            logger.info(destinationCredential.toString());
            d.setVfsDestCredential(destinationCredential);
        } else {
            OAuthEndpointCredential destinationCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getDestination().getType(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            d.setOauthDestCredential(destinationCredential);
        }
        List<EntityInfo> expandedFiles = selectAndExpand(s, odsTransferRequest.getSource().getInfoList());
        logger.info("After expansion service");
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
