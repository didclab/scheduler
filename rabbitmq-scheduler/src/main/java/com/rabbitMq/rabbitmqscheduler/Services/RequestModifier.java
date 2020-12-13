package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestModifier {

//    public TransferJobRequestWithMetaData modifyRequest(TransferJobRequestWithMetaData transferJobRequestWithMetaData){
//         TransferJobRequest.Source source = transferJobRequestWithMetaData.getSource();
//         TransferJobRequest.EntityInfo entityInfo = source.getInfo();
//         String path = entityInfo.getPath();
//         String[] pathArray = path.split("/");
//         entityInfo.setPath(pathArray[0]+"/");
//         source.setInfo(entityInfo);
//         transferJobRequestWithMetaData.setSource(source);
//         return transferJobRequestWithMetaData;
//    }

    public TransferJobRequest createRequest(RequestFromODS odsTransferRequest) {
        TransferJobRequest transferJobRequest = new TransferJobRequest("100", "vsingh27@buffalo.edu", 1, 64000);
        return transferJobRequest;
    }
}
