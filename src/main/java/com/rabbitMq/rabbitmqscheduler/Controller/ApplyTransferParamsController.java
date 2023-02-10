package com.rabbitMq.rabbitmqscheduler.Controller;

import com.rabbitMq.rabbitmqscheduler.DTO.TransferParams;
import com.rabbitMq.rabbitmqscheduler.Sender.MessageSender;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ApplyTransferParamsController {

    MessageSender messageSender;

    public ApplyTransferParamsController(MessageSender messageSender){
        this.messageSender = messageSender;
    }

    @PutMapping("/apply/application/params")
    public void consumeApplicationParamChange( @RequestBody TransferParams transferParams){
        this.messageSender.sendApplicationParams(transferParams, transferParams.getTransferNodeName());
    }
}
