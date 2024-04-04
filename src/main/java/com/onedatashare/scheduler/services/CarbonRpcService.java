package com.onedatashare.scheduler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onedatashare.scheduler.enums.MessageType;
import com.onedatashare.scheduler.model.CarbonIpEntry;
import com.onedatashare.scheduler.model.CarbonMeasureRequest;
import com.onedatashare.scheduler.model.CarbonMeasureResponse;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarbonRpcService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ods.rabbitmq.queue}")
    String queueName;

    @Value("${ods.rabbitmq.exchange}")
    String exchange;

    private Logger log = LoggerFactory.getLogger(CarbonRpcService.class);

    public CarbonRpcService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;

    }

    @SneakyThrows
    public CarbonMeasureResponse measureCarbon(CarbonMeasureRequest carbonMeasureRequest) {
        log.info("Sending Measure Carbon Request to: Exchange {} Routing Key {}", this.exchange, carbonMeasureRequest.transferNodeName);
        CarbonMeasureResponse carbonMeasureResponse = this.rabbitTemplate.convertSendAndReceiveAsType(this.exchange, carbonMeasureRequest.transferNodeName, carbonMeasureRequest, MessageSender.embedMessageType(MessageType.CARBON_AVG_REQUEST), new ParameterizedTypeReference<CarbonMeasureResponse>() {
        });
        log.info("Response: {}", carbonMeasureResponse);
        return carbonMeasureResponse;
    }

    public List<CarbonIpEntry> traceRoute(CarbonMeasureRequest carbonMeasureRequest) {
        log.info("Sending Measure Carbon Request to: Exchange {} Routing Key {}", this.exchange, carbonMeasureRequest.transferNodeName);
        return this.rabbitTemplate.convertSendAndReceiveAsType(this.exchange, carbonMeasureRequest.transferNodeName, carbonMeasureRequest, MessageSender.embedMessageType(MessageType.CARBON_IP_REQUEST), new ParameterizedTypeReference<List<CarbonIpEntry>>() {});
    }
}
