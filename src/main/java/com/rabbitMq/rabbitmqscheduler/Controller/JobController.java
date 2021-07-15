package com.rabbitMq.rabbitmqscheduler.Controller;

import com.rabbitMq.rabbitmqscheduler.DTO.transferFromODS.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.Sender.MessageSender;
import com.rabbitMq.rabbitmqscheduler.Services.FTPExpander;
import com.rabbitMq.rabbitmqscheduler.Services.RequestModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    MessageSender messageSender;

    @Autowired
    RequestModifier requestModifier;

    @RequestMapping(value = "/receiveRequest", method = RequestMethod.POST)
    public ResponseEntity<String> receiveRequest(@RequestBody RequestFromODS odsTransferRequest) {
        logger.info("Created message with owner " + odsTransferRequest.getOwnerId() +" and the job id is "
                + odsTransferRequest.getOptions().toString());
        TransferJobRequest transferJobRequest = requestModifier.createRequest(odsTransferRequest);
        messageSender.sendTransferRequest(transferJobRequest);
        return new ResponseEntity<>("Pushed job to queue with id " + transferJobRequest.getJobId(), HttpStatus.OK);
    }
}