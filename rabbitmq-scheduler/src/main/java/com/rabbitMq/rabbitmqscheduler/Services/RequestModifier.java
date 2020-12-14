package com.rabbitMq.rabbitmqscheduler.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.OAuthEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

@Service
public class RequestModifier {
    private static final Logger logger = LoggerFactory.getLogger(RequestModifier.class);

    @Value("${cred.service.uri}")
    String credBaseUri;


    Set<String> nonOautUsingType = new HashSet<>(Arrays.asList(new String[]{"ftp", "sftp", "http", "vfs"}));
//    Set<String> oautUsingType = new HashSet<>(Arrays.asList(new String[]{"s3", "dropbox", "box", "gdrive", "gftp"}));

    public TransferJobRequest createRequest(RequestFromODS odsTransferRequest) {
        logger.info("Creating request for Transfer Service");

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

        logger.info("Source type is : " + odsTransferRequest.getSource().getType());
        logger.info("Destination type is : " + odsTransferRequest.getDestination().getType());
        if (nonOautUsingType.contains(odsTransferRequest.getSource().getType().toString())) {
            AccountEndpointCredential sourceCred = getNonOautCred(odsTransferRequest.getUserId(), odsTransferRequest.getAccountId(), odsTransferRequest.getSource().getType());
            s.setVfsSourceCredential(sourceCred);
        } else {
            OAuthEndpointCredential sourceCred = getOautCred(odsTransferRequest.getUserId(), odsTransferRequest.getAccountId(), odsTransferRequest.getSource().getType());
            s.setOauthSourceCredential(sourceCred);
        }
        if (nonOautUsingType.contains(odsTransferRequest.getDestination().getType().toString())) {
            AccountEndpointCredential destCred = getNonOautCred(odsTransferRequest.getUserId(), odsTransferRequest.getAccountId(), odsTransferRequest.getDestination().getType());
            d.setVfsDestCredential(destCred);
        } else {
            OAuthEndpointCredential destCred = getOautCred(odsTransferRequest.getUserId(), odsTransferRequest.getAccountId(), odsTransferRequest.getDestination().getType());
            d.setOauthDestCredential(destCred);
        }

        transferJobRequest.setSource(s);
        transferJobRequest.setDestination(d);

        return transferJobRequest;
    }

    private OAuthEndpointCredential getOautCred(String userId, String accountId, EndPointType type) {
        logger.info("Getting Oauth cred from cred service for : " + userId);
        String urlToRead = credBaseUri + userId + "/" + type + "/" + accountId;
        OAuthEndpointCredential oAuthEndpointCredential = null;

        String jsonString = getResponseFromCred(urlToRead);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            oAuthEndpointCredential = objectMapper.readValue(jsonString, OAuthEndpointCredential.class);
        } catch (JsonProcessingException e) {
            logger.error("Not able to parse nonOauth cred json");
            e.printStackTrace();
        }
        return oAuthEndpointCredential;
    }

    private AccountEndpointCredential getNonOautCred(String userId, String accountId, EndPointType type) {
        logger.info("Geeting nonOauth cred from cred service for : " + userId);
        String urlToRead = credBaseUri + userId + "/" + type + "/" + accountId;
        AccountEndpointCredential accountEndpointCredential = null;

        String jsongString = getResponseFromCred(urlToRead);
        logger.info("jsonString is : " + jsongString);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            accountEndpointCredential = objectMapper.readValue(jsongString, AccountEndpointCredential.class);
        } catch (JsonProcessingException e) {
            logger.error("Not able to parse nonOauth cred json");
            e.printStackTrace();
        }
        return accountEndpointCredential;
    }

    private String getResponseFromCred(String urlToRead) {
        logger.info("Hitting cred service with url : " + urlToRead);
        StringBuilder line = new StringBuilder();
        try {
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != 200) {
                logger.error("Not able to retrive nonOauth cred");
                throw new RuntimeException("HttpResponseCode : " + conn.getResponseCode());
            } else {
                Scanner sc = new Scanner(url.openStream());
                while (sc.hasNext()) {
                    line.append(sc.nextLine());
                }
                sc.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line.toString();
    }
}
