package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.enums.MessageType;
import com.onedatashare.scheduler.model.TransferJobRequest;
import com.onedatashare.scheduler.model.TransferParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final ODSRouter odsRouter;

    @Value("${ods.default.nodes}")
    String odsNodesGroupName;

    public MessageSender(ODSRouter odsRouter){
        this.odsRouter = odsRouter;
    }


    public void routeTransferRequest(TransferJobRequest odsTransferRequest) {
        boolean sourceVfs = odsTransferRequest.getSource().getType().equals(EndPointType.vfs);
        boolean destVfs = odsTransferRequest.getDestination().getType().equals(EndPointType.vfs);
        String identity = "";
        if(odsTransferRequest.getTransferNodeName() != null && !odsTransferRequest.getTransferNodeName().isEmpty()){
            identity = odsTransferRequest.getTransferNodeName();
        }else if (sourceVfs || destVfs) {
            //for any vfs transfer where the user has their own transfer-service running on their metal.
            if (sourceVfs) {
                identity = odsTransferRequest.getSource().getCredId().toLowerCase();
            }
            if (destVfs) {
                identity = odsTransferRequest.getDestination().getCredId().toLowerCase();
            }
        } else {
            //for all transfers that are using the ODS backend
            identity = this.odsNodesGroupName;
        }
        this.odsRouter.sendPojo(odsTransferRequest, identity, MessageType.FILE_TRANSFER_REQUEST);
    }

    /**
     * The Transfer params to send using the routingKey
     * @param transferParams
     * @param identity
     */
    public void sendApplicationParams(TransferParams transferParams, String identity) {
        logger.info("Application Params: {} going to {}", transferParams, identity);
        this.odsRouter.sendPojo(transferParams, identity, MessageType.OPTIMIZATION_PARAM_CHANGE_REQUEST);
    }

}