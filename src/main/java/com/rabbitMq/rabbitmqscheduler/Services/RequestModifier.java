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
    Logger logger = LoggerFactory.getLogger(RequestModifier.class);

    @Autowired
    CredentialService credentialService;
    @Autowired
    SFTPExpander sftpExpander;
    @Autowired
    FTPExpander ftpExpander;
    @Autowired
    S3Expander s3Expander;
    @Autowired
    BoxExpander boxExpander;
    @Autowired
    DropBoxExpander dropBoxExpander;
    @Autowired
    HttpExpander httpExpander;

    @Autowired
    GDriveExpander gDriveExpander;

    Set<String> nonOautUsingType = new HashSet<>(Arrays.asList(new String[]{"ftp", "sftp", "http", "s3"}));
    Set<String> oautUsingType = new HashSet<>(Arrays.asList(new String[]{"dropbox", "box", "gdrive", "gftp"}));

    public List<EntityInfo> selectAndExpand(TransferJobRequest.Source source, List<EntityInfo> selectedResources) {
        logger.info("The info list in select and expand is \n" + selectedResources.toString());
        switch (source.getType()) {
            case ftp:
                ftpExpander.createClient(source.getVfsSourceCredential());
                return ftpExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case s3:
                s3Expander.createClient(source.getVfsSourceCredential());
                return s3Expander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case sftp:
            case scp:
                sftpExpander.createClient(source.getVfsSourceCredential());
                return sftpExpander.expandedFileSystem(selectedResources, source.getParentInfo().getPath());
            case http:
                httpExpander.createClient(source.getVfsSourceCredential());
                return httpExpander.expandedFileSystem(source.getInfoList(), source.getParentInfo().getPath());
            case box:
                boxExpander.createClient(source.getOauthSourceCredential());
                return boxExpander.expandedFileSystem(selectedResources, source.getParentInfo().getId());
            case dropbox:
                dropBoxExpander.createClient(source.getOauthSourceCredential());
                return dropBoxExpander.expandedFileSystem(selectedResources, source.getParentInfo().getId());
            case vfs:
                return selectedResources;
            case gdrive:
                gDriveExpander.createClient(source.getOauthSourceCredential());
                return gDriveExpander.expandedFileSystem(selectedResources, source.getParentInfo().getId());

        }
        return null;
    }

    /**
     * This method is supposed to take a list of Files that are expanded and make sure the chunk size being used is supported by the source/destination
     * So far all what I can tell is that Box has an "optimized" chunk size that we should use to write too.
     * @param entityInfo
     * @param destination
     * @param userChunkSize
     * @return List of Files that have the proper chunkSize to use during the transfer.
     */
    public List<EntityInfo> checkDestinationChunkSize(List<EntityInfo> entityInfo, TransferJobRequest.Destination destination, Integer userChunkSize){
        entityInfo.forEach(entityInfo1 -> {
            if(entityInfo1.getChunkSize() == 0){
                entityInfo1.setChunkSize(userChunkSize);
            }
        });
        switch (destination.getType()){
            case box:
                boxExpander.createClient(destination.getOauthDestCredential());
                return boxExpander.destinationChunkSize(entityInfo, destination.getParentInfo().getPath(), userChunkSize);
            case dropbox:
                return dropBoxExpander.destinationChunkSize(entityInfo, destination.getParentInfo().getPath(), userChunkSize);
            case ftp:
                return ftpExpander.destinationChunkSize(entityInfo, destination.getParentInfo().getPath(), userChunkSize);
            case sftp:
            case scp:
                return sftpExpander.destinationChunkSize(entityInfo, destination.getParentInfo().getPath(), userChunkSize);
            case s3:
                return s3Expander.destinationChunkSize(entityInfo, destination.getParentInfo().getPath(), userChunkSize);
            case http:
                return httpExpander.destinationChunkSize(entityInfo, destination.getParentInfo().getPath(), userChunkSize);
        }

        return entityInfo;
    }

    public TransferJobRequest createRequest(RequestFromODS odsTransferRequest) {
        logger.info(odsTransferRequest.toString());
        TransferJobRequest transferJobRequest = new TransferJobRequest();
        transferJobRequest.setJobId("1");//We will neeed to have some kind of ID system so that we always provide unique keys, an easy way is to just use the current nano time plus the total number of jobs processed.
        transferJobRequest.setOptions(TransferOptions.createTransferOptionsFromUser(odsTransferRequest.getOptions()));
        transferJobRequest.setOwnerId(odsTransferRequest.getOwnerId());
        transferJobRequest.setPriority(1);//need some way of creating priority depending on factors. Memberyship type? Urgency of transfer, prob need create these groups

        TransferJobRequest.Source s = new TransferJobRequest.Source();
        s.setCredId(odsTransferRequest.getSource().getCredId());
        s.setParentInfo(odsTransferRequest.getSource().getParentInfo());
        s.setType(odsTransferRequest.getSource().getType());

        TransferJobRequest.Destination d = new TransferJobRequest.Destination();
        d.setParentInfo(odsTransferRequest.getDestination().getParentInfo());
        d.setCredId(odsTransferRequest.getDestination().getCredId());
        d.setType(odsTransferRequest.getDestination().getType());

        if (nonOautUsingType.contains(odsTransferRequest.getSource().getType().toString())) {
            AccountEndpointCredential sourceCredential = credentialService.fetchAccountCredential(odsTransferRequest.getSource().getType().toString(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            s.setVfsSourceCredential(sourceCredential);
        } else if (oautUsingType.contains(odsTransferRequest.getSource().getType().toString())) {
            OAuthEndpointCredential sourceCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getSource().getType(), odsTransferRequest.getOwnerId(), odsTransferRequest.getSource().getCredId());
            s.setOauthSourceCredential(sourceCredential);
        }
        if (nonOautUsingType.contains(odsTransferRequest.getDestination().getType().toString())) {
            AccountEndpointCredential destinationCredential = credentialService.fetchAccountCredential(odsTransferRequest.getDestination().getType().toString(), odsTransferRequest.getOwnerId(), odsTransferRequest.getDestination().getCredId());
            d.setVfsDestCredential(destinationCredential);
        } else if (oautUsingType.contains(odsTransferRequest.getDestination().getType().toString())) {
            OAuthEndpointCredential destinationCredential = credentialService.fetchOAuthCredential(odsTransferRequest.getDestination().getType(), odsTransferRequest.getOwnerId(), odsTransferRequest.getDestination().getCredId());
            d.setOauthDestCredential(destinationCredential);
        }
        List<EntityInfo> expandedFiles = this.selectAndExpand(s, odsTransferRequest.getSource().getInfoList());
        expandedFiles = this.checkDestinationChunkSize(expandedFiles, d, odsTransferRequest.getOptions().getChunkSize());
        s.setInfoList(expandedFiles);
        transferJobRequest.setSource(s);
        transferJobRequest.setDestination(d);
        transferJobRequest.setChunkSize(correctChunkSize(transferJobRequest.getDestination().getType(), odsTransferRequest.getOptions().getChunkSize()));//this is default and needs to come from the optimizer
        return transferJobRequest;
    }

    /**
     *
     * Current little hack to make sure writing to S3 results in a chunkSize greater than 5MB if a multipart request.
     * This should be a property that is data mined in the optimization service
     * This method should not be used and is only kept here to support the compatability with an API change where chunkSize was set for the entire transfer and not per file basis.
     * @param destType
     * @param chunkSize
     * @return
     */
    @Deprecated
    public int correctChunkSize(EndPointType destType, int chunkSize) {
        if (destType.equals(EndPointType.s3) && chunkSize < 5000000) { //5MB as we work with bytes not bits!
            return 10000000;
        } else if (destType.equals(EndPointType.gdrive) && chunkSize < 5000000) {
            return 10000000;
        } else {
            return chunkSize;
        }
    }
}
