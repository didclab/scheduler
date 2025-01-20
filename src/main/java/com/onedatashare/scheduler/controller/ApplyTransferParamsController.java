package com.onedatashare.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onedatashare.scheduler.enums.MessageType;
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
    public ResponseEntity<Object> consumeApplicationParamChange(@RequestBody TransferParams transferParams) {
        try {
            this.messageSender.sendMessage(transferParams, MessageType.APPLICATION_PARAM_CHANGE, transferParams.getTransferNodeName());
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
