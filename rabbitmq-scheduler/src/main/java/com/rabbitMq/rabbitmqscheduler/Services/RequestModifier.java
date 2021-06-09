package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferOptions;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
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
    @Autowired
    BoxExpander boxExpander;

    Set<String> nonOautUsingType = new HashSet<>(Arrays.asList(new String[]{"ftp", "sftp", "http", "vfs", "s3"}));
//    Set<String> oautUsingType = new HashSet<>(Arrays.asList(new String[]{ "dropbox", "box", "gdrive", "gftp"}));

    public List<EntityInfo> selectAndExpand(TransferJobRequest.Source source, List<EntityInfo> selectedResources){
        switch (source.getType()){
            case ftp:
                ftpExpander.createClient(source.getVfsSourceCredential());
                return ftpExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case s3:
                s3Expander.createClient(source.getVfsSourceCredential());
                return s3Expander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case sftp:
                sftpExpander.createClient(source.getVfsSourceCredential());
                return sftpExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case box:
                boxExpander.createClient(source.getOauthSourceCredential());
                return boxExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
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
        TransferJobRequest transferJobRequest = new TransferJobRequest();
        transferJobRequest.setJobId("1");//We will neeed to have some kind of ID system so that we always provide unique keys, an easy way is to just use the current nano time plus the total number of jobs processed.
        transferJobRequest.setOptions(TransferOptions.createTransferOptionsFromUser(odsTransferRequest.getOptions()));
        transferJobRequest.setOwnerId(odsTransferRequest.getOwnerId());
        transferJobRequest.setPriority(1);//need some way of creating priority depending on factors. Memberyship type? Urgency of transfer, prob need create these groups
        TransferJobRequest.Source s = new TransferJobRequest.Source();
        s.setInfoList(odsTransferRequest.getSource().getInfoList());
        s.setParentInfo(odsTransferRequest.getSource().getParentInfo());
        s.setType(odsTransferRequest.getSource().getType());
        TransferJobRequest.Destination d = new TransferJobRequest.Destination();
        d.setParentInfo(odsTransferRequest.getDestination().getParentInfo());
        d.setType(odsTransferRequest.getDestination().getType());
        if (nonOautUsingType.contains(odsTransferRequest.getSource().getType().toString())) {
            AccountEndpointCredential sourceCredential =credentialService.fetchAccountCredential(odsTransferRequest.getSource().getType().toString(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            s.setVfsSourceCredential(sourceCredential);
        } else {
            OAuthEndpointCredential sourceCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getSource().getType(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            s.setOauthSourceCredential(sourceCredential);
        }
        if (nonOautUsingType.contains(odsTransferRequest.getDestination().getType().toString())) {
            AccountEndpointCredential destinationCredential =  credentialService.fetchAccountCredential(odsTransferRequest.getDestination().getType().toString(), odsTransferRequest.getOwnerId(), odsTransferRequest.getDestination().getCredId());
            d.setVfsDestCredential(destinationCredential);
        } else {
            OAuthEndpointCredential destinationCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getDestination().getType(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            d.setOauthDestCredential(destinationCredential);
        }
        List<EntityInfo> expandedFiles = selectAndExpand(s, odsTransferRequest.getSource().getInfoList());
        s.setInfoList(expandedFiles);
        transferJobRequest.setSource(s);
        transferJobRequest.setDestination(d);
        transferJobRequest.setChunkSize(correctChunkSize(transferJobRequest.getDestination().getType(), 6400000));//this is default and needs to come from the optimizer
        return transferJobRequest;
    }

    /**
     * Current little hack to make sure writing to S3 results in a chunkSize greater than 5MB if a multipart request.
     * This should be a property that is data mined in the optimization service
     * @param destType
     * @param chunkSize
     * @return
     */
    public int correctChunkSize(EndPointType destType, int chunkSize){
        if(destType.equals(EndPointType.s3) && chunkSize < 5000000){ //5MB as we work with bytes not bits!
            return 10000000;
        }else{
            return chunkSize;
        }

    }
}
