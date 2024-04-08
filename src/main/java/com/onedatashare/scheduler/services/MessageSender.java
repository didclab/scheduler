package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.MessageType;
import com.onedatashare.scheduler.model.TransferJobRequest;
import com.onedatashare.scheduler.model.TransferParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    RabbitTemplate rabbitTemplate;

    DirectExchange directExchange;

    @Value("${ods.rabbitmq.exchange}")
    private String exchange;

    public MessageSender(RabbitTemplate rmqTemplate, DirectExchange directExchange) {
        this.rabbitTemplate = rmqTemplate;
        this.directExchange = directExchange;
    }


    public void sendTransferRequest(TransferJobRequest odsTransferRequest) {
        MessagePostProcessor postProcessor = MessageSender.embedMessageType(MessageType.TRANSFER_JOB_REQUEST);
        rabbitTemplate.convertAndSend(exchange, odsTransferRequest.getTransferNodeName(), odsTransferRequest, postProcessor);
        logger.info("Processed Job: {}", odsTransferRequest);
    }

    /**
     * The Transfer params to send using the routingKey
     *
     * @param transferParams
     * @param routingKey
     */
    public void sendApplicationParams(TransferParams transferParams, String routingKey) {
        logger.info("Application Params: {} going to {}", transferParams, routingKey);
        MessagePostProcessor postProcessor = MessageSender.embedMessageType(MessageType.APPLICATION_PARAM_CHANGE);
        this.rabbitTemplate.convertAndSend(routingKey, transferParams, postProcessor);
    }

    public static MessagePostProcessor embedMessageType(MessageType type) {
        return message -> {
            message.getMessageProperties().getHeaders().put("type", type);
            return message;
        };
    }

}