package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.model.TransferJobRequest;
import com.onedatashare.scheduler.model.TransferParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    AmqpTemplate rmqTemplate;

    @Autowired
    DirectExchange directExchange;

    @Value("${ods.rabbitmq.queue}")
    private String queueName;

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    @Value("${ods.rabbitmq.routingkey}")
    private String routingKey;


    public void sendTransferRequest(TransferJobRequest odsTransferRequest) {
        logger.debug(odsTransferRequest.toString());
        boolean sourceVfs = odsTransferRequest.getSource().getType().equals(EndPointType.vfs);
        boolean destVfs = odsTransferRequest.getDestination().getType().equals(EndPointType.vfs);
        if( odsTransferRequest.getTransferNodeName() != null && !odsTransferRequest.getTransferNodeName().isEmpty()){
            routingKey = odsTransferRequest.getTransferNodeName();
            rmqTemplate.convertAndSend(exchange, routingKey, odsTransferRequest);
        }else if (sourceVfs || destVfs) {
            //for any vfs transfer where the user has their own transfer-service running on their metal.
            String routingKey = this.routingKey;
            if (sourceVfs) {
                routingKey = odsTransferRequest.getSource().getCredId().toLowerCase();
            }
            if (destVfs) {
                routingKey = odsTransferRequest.getDestination().getCredId().toLowerCase();
            }
            logger.info("Vfs Request: user={}, routeKey={}", odsTransferRequest.getOwnerId(), routingKey);
            rmqTemplate.convertAndSend(exchange, routingKey, odsTransferRequest);
        } else {
            //for all transfers that are using the ODS backend
            logger.info("Ods Request: user={}, routeKey={}", odsTransferRequest.getOwnerId(), queueName);
            rmqTemplate.convertAndSend(exchange, queueName, odsTransferRequest);
        }
        logger.info("Processed Job: {}", odsTransferRequest);
    }

    /**
     * The Transfer params to send using the routingKey
     * @param transferParams
     * @param routingKey
     */
    public void sendApplicationParams(TransferParams transferParams, String routingKey) {
        logger.info("Application Params: {} going to {}", transferParams, routingKey);
        this.rmqTemplate.convertAndSend(routingKey, transferParams);
    }

}