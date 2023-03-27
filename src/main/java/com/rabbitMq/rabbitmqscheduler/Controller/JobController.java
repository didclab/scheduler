package com.rabbitMq.rabbitmqscheduler.Controller;

import com.rabbitMq.rabbitmqscheduler.DTO.StopRequest;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobResponse;
import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Sender.MessageSender;
import com.rabbitMq.rabbitmqscheduler.Services.RequestModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    MessageSender messageSender;

    @Autowired
    RequestModifier requestModifier;

    @PostMapping(value = "/receiveRequest")
    public TransferJobResponse receiveRequest(@RequestBody RequestFromODS odsTransferRequest) {
        TransferJobRequest transferJobRequest = requestModifier.createRequest(odsTransferRequest);
        logger.info(transferJobRequest.toString());
        messageSender.sendTransferRequest(transferJobRequest, odsTransferRequest.getSource(), odsTransferRequest.getDestination());
        TransferJobResponse response = new TransferJobResponse();
        response.setMessage("Job Submitted");
        return response;
    }
    @PostMapping(value = "/stopJob")
    public Boolean stopJob(@RequestBody StopRequest stopRequest) {
        logger.info("Received request to stop job with id: " + stopRequest.toString());
        messageSender.sendStopJobRequest(stopRequest);
        return true;
    }

}