package com.rabbitMq.rabbitmqscheduler.Sender;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    private RabbitTemplate rmqTemplate;

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    @Value("${ods.rabbitmq.routingkey}")
    private String routingkey;

    public void sendTransferRequest(TransferJobRequest odsTransferRequest) {
        rmqTemplate.convertAndSend(exchange, routingkey, odsTransferRequest);
        System.out.println("Send msg = " + odsTransferRequest.toString());
    }
}