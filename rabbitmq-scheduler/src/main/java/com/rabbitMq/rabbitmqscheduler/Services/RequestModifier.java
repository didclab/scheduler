package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequestWithMetaData;
import org.springframework.stereotype.Service;

@Service
public class RequestModifier {

    public TransferJobRequestWithMetaData modifyRequest(TransferJobRequestWithMetaData transferJobRequestWithMetaData){
         TransferJobRequest.Source source = transferJobRequestWithMetaData.getSource();
         TransferJobRequest.EntityInfo entityInfo = source.getInfo();
         String path = entityInfo.getPath();
         String[] pathArray = path.split("/");
         entityInfo.setPath(pathArray[0]+"/");
         source.setInfo(entityInfo);
         transferJobRequestWithMetaData.setSource(source);
         return transferJobRequestWithMetaData;
    }
}
