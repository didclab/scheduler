package com.rabbitMq.rabbitmqscheduler.Sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequestWithMetaData;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferRequest;
import com.rabbitMq.rabbitmqscheduler.Message.MqMessage;
import com.rabbitMq.rabbitmqscheduler.RabbitmqSchedulerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final RabbitTemplate rabbitTemplate;

    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

//    @Scheduled(fixedDelay = 3000L)
//    public void sendPracticalMessage() {
//        MqMessage tip = new MqMessage(null, 1, false);
//        rabbitTemplate.convertAndSend(RabbitmqSchedulerApplication.EXCHANGE_NAME, RabbitmqSchedulerApplication.ROUTING_KEY, tip);
//        log.info("Practical Tip sent");
//    }

    public void sendTransferRequest(TransferJobRequestWithMetaData transferRequest){
        transferRequest.id= "100";
        Gson gson = new Gson();
        String json = gson.toJson(transferRequest);
        MqMessage message = new MqMessage(json,1,false);
        rabbitTemplate.convertAndSend(RabbitmqSchedulerApplication.EXCHANGE_NAME, RabbitmqSchedulerApplication.ROUTING_KEY, message);
        log.info("TransferRequest sent");
        log.info(json);
    }
}