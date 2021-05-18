package com.rabbitMq.rabbitmqscheduler.Controller;

import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Sender.MessageSender;
import com.rabbitMq.rabbitmqscheduler.Services.FTPExpander;
import com.rabbitMq.rabbitmqscheduler.Services.RequestModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    @Autowired
    MessageSender messageSender;

    @Autowired
    RequestModifier requestModifier;

    @PostMapping(value = "/receiveRequest")
    public String receiveRequest(@RequestBody RequestFromODS odsTransferRequest) {
        TransferJobRequest transferJobRequest = requestModifier.createRequest(odsTransferRequest);
        messageSender.sendTransferRequest(transferJobRequest);
        return "Message pushed to queue";
    }
}