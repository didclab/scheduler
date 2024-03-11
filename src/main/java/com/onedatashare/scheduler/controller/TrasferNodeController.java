package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.TransferParams;
import com.onedatashare.scheduler.services.MessageSender;
import com.onedatashare.scheduler.services.ODSRouter;
import com.onedatashare.scheduler.services.TransferNodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TrasferNodeController {


    private final MessageSender messageSender;
    private final TransferNodeService transferNodeService;

    public TrasferNodeController(MessageSender messageSender, TransferNodeService transferNodeService) {
        this.messageSender = messageSender;
        this.transferNodeService = transferNodeService;
    }

    @PutMapping("/apply/application/params")
    public ResponseEntity<String> consumeApplicationParamChange(@RequestBody TransferParams transferParams) {
        this.messageSender.sendApplicationParams(transferParams, transferParams.getTransferNodeName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/node/list")
    public ResponseEntity<List<String>> listUserODSConnectors(@RequestParam String userName){
        return ResponseEntity.ok(transferNodeService.listUserTransferNodes(userName));
    }

}
