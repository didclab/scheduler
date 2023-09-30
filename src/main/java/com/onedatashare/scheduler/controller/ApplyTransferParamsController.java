package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.TransferParams;
import com.onedatashare.scheduler.services.MessageSender;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ApplyTransferParamsController {

    MessageSender messageSender;

    public ApplyTransferParamsController(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @PutMapping("/apply/application/params")
    public ResponseEntity<String> consumeApplicationParamChange(@RequestBody TransferParams transferParams) {
        this.messageSender.sendApplicationParams(transferParams, transferParams.getTransferNodeName());
        return ResponseEntity.ok().build();
    }
}
