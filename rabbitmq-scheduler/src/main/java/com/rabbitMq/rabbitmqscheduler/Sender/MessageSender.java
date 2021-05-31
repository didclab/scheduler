package com.rabbitMq.rabbitmqscheduler.Sender;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    private RabbitTemplate rmqTemplate;

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    @Value("${ods.rabbitmq.routingkey}")
    private String routingkey;

    public void sendTransferRequest(TransferJobRequest odsTransferRequest) {
        rmqTemplate.convertAndSend(exchange, routingkey, odsTransferRequest);
        logger.info("Processed Job with ID: " + odsTransferRequest.getJobId());

    }
}