package com.rabbitMq.rabbitmqscheduler.Sender;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
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

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    @Value("${ods.rabbitmq.routingkey}")
    private String routingkey;

    public void sendTransferRequest(TransferJobRequest odsTransferRequest) {
        //all connector requests specify a different routing key which should be the email of that users queue.
        logger.info(odsTransferRequest.toString());
        if(odsTransferRequest.getSource().getType().equals(EndPointType.vfs) || odsTransferRequest.getDestination().getType().equals(EndPointType.vfs)){
            //for any connector transfer where the user has their own queue.
            String userNotEmail = odsTransferRequest.getOwnerId().split("@")[0];
            String destinationTransferType = odsTransferRequest.getDestination().getType().name();
            //String rKey = userNotEmail + "-Binding";
            String rKey = String.format("%s-%s-Binding", userNotEmail, destinationTransferType);
            String queueName = userNotEmail+ "-Queue";
            establishConnectorQueue(rKey, rKey);
            logger.info("User email prefix is "+userNotEmail+" and the routeKey is "+rKey+" and the queueName for our messages is " + queueName);
            rmqTemplate.convertAndSend(exchange, rKey, odsTransferRequest);
        }else{
            //for all aws tranfsers
            rmqTemplate.convertAndSend(exchange, routingkey, odsTransferRequest);
        }
        logger.info("Processed Job with ID: " + odsTransferRequest.getJobId());
    }

    public Queue createConnectorQueue(String queueName){
        Queue queue = new Queue(queueName, true);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    public void establishConnectorQueue(String queueName, String rKey){
        Binding binding = BindingBuilder.bind(createConnectorQueue(queueName))
                .to(directExchange)
                .with(rKey);
        amqpAdmin.declareBinding(binding);
    }

}