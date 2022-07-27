package com.rabbitMq.rabbitmqscheduler.Sender;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    AmqpAdmin amqpAdmin;

    @Value("${ods.rabbitmq.queue}")
    private String queueName;

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    @Value("${ods.rabbitmq.routingkey}")
    private String routingkey;

    public void sendTransferRequest(TransferJobRequest odsTransferRequest, RequestFromODS.@NonNull Source source, RequestFromODS.@NonNull Destination destination) {
        logger.debug(odsTransferRequest.toString());
        boolean sourceVfs = odsTransferRequest.getSource().getType().equals(EndPointType.vfs);
        boolean destVfs = odsTransferRequest.getDestination().getType().equals(EndPointType.vfs);
        if(sourceVfs || destVfs){
            //for any vfs transfer where the user has their own transfer-service running on their host.
            String queueName = "";
            String rKey = queueName;
            if(sourceVfs){
                queueName = source.getCredId().toLowerCase();
            }
            if (destVfs){
                queueName = destination.getCredId().toLowerCase();
            }
            establishConnectorQueue(queueName, rKey);
            logger.info("User email prefix is "+odsTransferRequest.getOwnerId()+" and the routeKey is "+rKey+" and the queueName for our messages is " + queueName);
            rmqTemplate.convertAndSend(exchange, queueName, odsTransferRequest);
        }else{
            //for all transfers that are using the ODS backend
            logger.info("User email prefix is "+ odsTransferRequest.getOwnerId()+" and the queueName for our messages is " + queueName);
            rmqTemplate.convertAndSend(exchange, this.queueName, odsTransferRequest);
        }
        logger.info("Processed Job with ID: " + odsTransferRequest.getJobId());
    }

    public void establishConnectorQueue(String queueName, String rKey){
        Queue queue = new Queue(queueName, true, false, true);
        amqpAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue)
                .to(directExchange)
                .with(rKey);
        amqpAdmin.declareBinding(binding);
    }

}