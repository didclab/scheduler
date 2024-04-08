package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.MessageType;
import com.onedatashare.scheduler.model.carbon.CarbonIpEntry;
import com.onedatashare.scheduler.model.carbon.CarbonMeasureRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarbonRpcService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${ods.rabbitmq.queue}")
    String queueName;

    @Value("${ods.rabbitmq.exchange}")
    String exchange;

    private Logger log = LoggerFactory.getLogger(CarbonRpcService.class);

    public CarbonRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;

    }

    public List<CarbonIpEntry> traceRoute(CarbonMeasureRequest carbonMeasureRequest) {
        log.info("Sending Measure Carbon Request to: Exchange {} Routing Key {}", this.exchange, carbonMeasureRequest.transferNodeName);
        return this.rabbitTemplate.convertSendAndReceiveAsType(this.exchange, carbonMeasureRequest.transferNodeName, carbonMeasureRequest, MessageSender.embedMessageType(MessageType.CARBON_IP_REQUEST), new ParameterizedTypeReference<List<CarbonIpEntry>>() {
        });
    }

    public Double averageCarbonIntensityOfTraceRoute(List<CarbonIpEntry> ips) {
        return ips.stream().collect(Collectors.averagingDouble(CarbonIpEntry::getCarbonIntensity));
    }
}
