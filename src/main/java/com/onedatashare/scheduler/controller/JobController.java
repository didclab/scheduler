package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.model.TransferJobRequest;
import com.onedatashare.scheduler.services.MessageSender;
import com.onedatashare.scheduler.services.RequestModifier;
import com.onedatashare.scheduler.services.TransferNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
public class JobController {

    private final TransferNodeService transferNodeService;
    MessageSender messageSender;

    RequestModifier requestModifier;
    Logger logger = LoggerFactory.getLogger(JobController.class);

    public JobController(MessageSender messageSender, RequestModifier requestModifier, TransferNodeService transferNodeService) {
        this.requestModifier = requestModifier;
        this.messageSender = messageSender;
        this.transferNodeService = transferNodeService;
    }

    @PostMapping("/job/schedule")
    public ResponseEntity<UUID> scheduleJob(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobStartTime, @RequestBody RequestFromODSDTO transferRequest) {
        logger.info(transferRequest.toString());
        UUID id = UUID.randomUUID();
        RequestFromODS transferJob = new RequestFromODS();
        transferJob.setTransferNodeName(transferRequest.getTransferNodeName());
        transferJob.setOptions(transferRequest.getOptions());
        transferJob.setDestination(transferRequest.getDestination());
        transferJob.setSource(transferRequest.getSource());
        transferJob.setOwnerId(transferRequest.getOwnerId());
        transferJob.setJobUuid(id);
        transferJob.setJobStartTime(jobStartTime);
        TransferJobRequest transferJobRequest = this.requestModifier.createRequest(transferJob);
        this.messageSender.routeTransferRequest(transferJobRequest);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/job/user")
    public ResponseEntity<RequestFromODS> listUserJobs(@RequestParam String userName){
        this.transferNodeService.listUserTransferNodes(userName);
    }
    @GetMapping("/job/details")
    public ResponseEntity<RequestFromODS> getScheduledJob(@RequestParam UUID jobUuid, @RequestParam String transferNodeName) {

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/job/delete")
    public ResponseEntity<Void> deleteScheduledJob(@RequestParam UUID jobUuid) {
        return ResponseEntity.ok(null);

    }

    @PostMapping(value = "/job/run")
    public ResponseEntity<UUID> runJob(@RequestBody RequestFromODSDTO odsTransferRequest) {
        return this.scheduleJob(LocalDateTime.now(), odsTransferRequest);
    }

}