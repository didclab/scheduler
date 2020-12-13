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
    private AmqpTemplate rmqTemplate;

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    @Value("${ods.rabbitmq.routingkey}")
    private String routingkey;

    public void sendTransferRequest(TransferJobRequest odsTransferRequest) {

        rmqTemplate.convertAndSend(exchange, routingkey, odsTransferRequest);
        System.out.println("Send msg = " + odsTransferRequest);
//        Gson gson = new Gson();
//        String json = gson.toJson(transferRequest);
//        MqMessage message = new MqMessage(json,1,false);
//        rabbitTemplate.convertAndSend(RabbitmqSchedulerApplication.EXCHANGE_NAME, RabbitmqSchedulerApplication.ROUTING_KEY, message);
//        log.info("TransferRequest sent");
//        log.info(json);
    }
}