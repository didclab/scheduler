package com.rabbitMq.rabbitmqscheduler.Controller;

import com.rabbitMq.rabbitmqscheduler.DTO.ResultDTO;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequestWithMetaData;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferRequest;
import com.rabbitMq.rabbitmqscheduler.Sender.MessageSender;
import com.rabbitMq.rabbitmqscheduler.Services.CredentialService;
import com.rabbitMq.rabbitmqscheduler.Services.RequestModifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController {
    @Autowired
    MessageSender messageSender;
    @Autowired
    RequestModifier requestModifier;
    @Autowired
    CredentialService credentialService;

    @RequestMapping(value = "/receiveRequest",method = RequestMethod.POST)
    public ResultDTO receiveRequest(@RequestBody TransferJobRequestWithMetaData transferRequest){
        transferRequest = requestModifier.modifyRequest(transferRequest);
        transferRequest = credentialService.addCredentials(transferRequest);
        messageSender.sendTransferRequest(transferRequest);
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus("1");
        resultDTO.setResult("Message pushed to queue seuccesfully");
        return resultDTO;
    }
}
