package com.rabbitMq.rabbitmqscheduler.Sender;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferParams;
import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import lombok.NonNull;
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

    public void sendTransferRequest(TransferJobRequest odsTransferRequest, RequestFromODS.@NonNull Source source, RequestFromODS.@NonNull Destination destination) {
        logger.debug(odsTransferRequest.toString());
        boolean sourceVfs = odsTransferRequest.getSource().getType().equals(EndPointType.vfs);
        boolean destVfs = odsTransferRequest.getDestination().getType().equals(EndPointType.vfs);
        if (sourceVfs || destVfs) {
            //for any vfs transfer where the user has their own transfer-service running on their metal.
            String routingKey = this.routingKey;
            if (sourceVfs) {
                routingKey = source.getCredId().toLowerCase();
            }
            if (destVfs) {
                routingKey = destination.getCredId().toLowerCase();
            }
            logger.info("Vfs Request: user={}, routeKey={}", odsTransferRequest.getOwnerId(), routingKey);
            rmqTemplate.convertAndSend(exchange, routingKey, odsTransferRequest);
        } else {
            //for all transfers that are using the ODS backend
            logger.info("Ods Request: user={}, routeKey={}", odsTransferRequest.getOwnerId(), queueName);
            rmqTemplate.convertAndSend(exchange, routingKey, odsTransferRequest);
        }
        logger.info("Processed Job with ID: " + odsTransferRequest.getJobId());
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