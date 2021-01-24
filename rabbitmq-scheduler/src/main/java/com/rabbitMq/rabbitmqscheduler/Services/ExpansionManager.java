package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Services.expanders.FtpExpander;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
public class ExpansionManager {

    @Autowired
    FtpExpander ftpExpander;

    public TransferJobRequest expandedTransferJobRequest(TransferJobRequest transferJobRequest){

        switch (transferJobRequest.getSource().getType()){
            case ftp:
                try{
                    transferJobRequest.getSource().setInfoList(ftpExpander.expandFtpPath(transferJobRequest.getSource()));
                } catch (FileSystemException e){
                    e.printStackTrace();
                }
                break;
            case sftp:
                break;
        }
        return transferJobRequest;
    }
}
