package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.model.*;
import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.model.credential.OAuthEndpointCredential;
import com.onedatashare.scheduler.services.expanders.*;
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
                return ftpExpander.expandedFileSystem(selectedResources, source.getFileSourcePath());
            case s3:
                s3Expander.createClient(source.getVfsSourceCredential());
                return s3Expander.expandedFileSystem(selectedResources, source.getFileSourcePath());
            case sftp:
            case scp:
                sftpExpander.createClient(source.getVfsSourceCredential());
                return sftpExpander.expandedFileSystem(selectedResources, source.getFileSourcePath());
            case http:
                httpExpander.createClient(source.getVfsSourceCredential());
                return httpExpander.expandedFileSystem(selectedResources, source.getFileSourcePath());
            case box:
                boxExpander.createClient(source.getOauthSourceCredential());
                return boxExpander.expandedFileSystem(selectedResources, source.getFileSourcePath());
            case dropbox:
                dropBoxExpander.createClient(source.getOauthSourceCredential());
                return dropBoxExpander.expandedFileSystem(selectedResources, source.getFileSourcePath());
            case vfs:
                return selectedResources;
            case gdrive:
                gDriveExpander.createClient(source.getOauthSourceCredential());
                return gDriveExpander.expandedFileSystem(selectedResources, source.getFileSourcePath());

        }
        return null;
    }

    /**
     * This method is supposed to take a list of Files that are expanded and make sure the chunk size being used is supported by the source/destination
     * So far all what I can tell is that Box has an "optimized" chunk size that we should use to write too.
     *
     * @param entityInfo
     * @param destination
     * @param userChunkSize
     * @return List of Files that have the proper chunkSize to use during the transfer.
     */
    public List<EntityInfo> checkDestinationChunkSize(List<EntityInfo> entityInfo, TransferJobRequest.Destination destination, Integer userChunkSize) {
        entityInfo.forEach(entityInfo1 -> {
            if (entityInfo1.getChunkSize() == 0) {
                entityInfo1.setChunkSize(userChunkSize);
            }
        });
        switch (destination.getType()) {
            case box:
                boxExpander.createClient(destination.getOauthDestCredential());
                if(destination.getFileDestinationPath() == null || destination.getFileDestinationPath().isEmpty()){
                    destination.setFileDestinationPath("0");
                }
                return boxExpander.destinationChunkSize(entityInfo, destination.getFileDestinationPath(), userChunkSize);
            case dropbox:
                return dropBoxExpander.destinationChunkSize(entityInfo, destination.getFileDestinationPath(), userChunkSize);
            case ftp:
                return ftpExpander.destinationChunkSize(entityInfo, destination.getFileDestinationPath(), userChunkSize);
            case sftp:
            case scp:
                return sftpExpander.destinationChunkSize(entityInfo, destination.getFileDestinationPath(), userChunkSize);
            case s3:
                return s3Expander.destinationChunkSize(entityInfo, destination.getFileDestinationPath(), userChunkSize);
            case http:
                return httpExpander.destinationChunkSize(entityInfo, destination.getFileDestinationPath(), userChunkSize);
        }

        return entityInfo;
    }

    public TransferJobRequest createRequest(RequestFromODS odsTransferRequest) {
        logger.info(odsTransferRequest.toString());
        TransferJobRequest transferJobRequest = new TransferJobRequest();
        transferJobRequest.setOptions(TransferOptions.createTransferOptionsFromUser(odsTransferRequest.getOptions()));
        transferJobRequest.setOwnerId(odsTransferRequest.getOwnerId());
        transferJobRequest.setJobUuid(odsTransferRequest.getJobUuid());
        TransferJobRequest.Source s = new TransferJobRequest.Source();
        s.setCredId(odsTransferRequest.getSource().getCredId());
        s.setFileSourcePath(odsTransferRequest.getSource().getFileSourcePath());
        s.setType(odsTransferRequest.getSource().getType());

        TransferJobRequest.Destination d = new TransferJobRequest.Destination();
        d.setFileDestinationPath(odsTransferRequest.getDestination().getFileDestinationPath());
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
        List<EntityInfo> expandedFiles = this.selectAndExpand(s, odsTransferRequest.getSource().getResourceList());
        logger.info("Expanded files: {}", expandedFiles);
        expandedFiles = this.checkDestinationChunkSize(expandedFiles, d, odsTransferRequest.getOptions().getChunkSize());
        s.setInfoList(expandedFiles);
        transferJobRequest.setSource(s);
        transferJobRequest.setDestination(d);
        transferJobRequest.setTransferNodeName(odsTransferRequest.getTransferNodeName());
        return transferJobRequest;
    }

    /**
     * Current little hack to make sure writing to S3 results in a chunkSize greater than 5MB if a multipart request.
     * This should be a property that is data mined in the optimization service
     * This method should not be used and is only kept here to support the compatability with an API change where chunkSize was set for the entire transfer and not per file basis.
     *
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
