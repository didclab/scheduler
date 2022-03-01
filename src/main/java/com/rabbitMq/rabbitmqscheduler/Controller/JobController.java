package com.rabbitMq.rabbitmqscheduler.Controller;

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
        logger.info("Recieved message with id " + odsTransferRequest.getOwnerId());
        TransferJobRequest transferJobRequest = requestModifier.createRequest(odsTransferRequest);
        messageSender.sendTransferRequest(transferJobRequest, odsTransferRequest.getSource(), odsTransferRequest.getDestination());
        logger.info(transferJobRequest.toString());
        TransferJobResponse response = new TransferJobResponse();
        response.setId(transferJobRequest.getJobId());//this will need to be read in from CDB?
        response.setMessage("Job Submitted");
        return response;
    }
}