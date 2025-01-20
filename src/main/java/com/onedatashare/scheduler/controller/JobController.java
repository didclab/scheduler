package com.onedatashare.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onedatashare.scheduler.enums.MessageType;
import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.model.TransferJobRequest;
import com.onedatashare.scheduler.services.JobScheduler;
import com.onedatashare.scheduler.services.MessageSender;
import com.onedatashare.scheduler.services.RequestModifier;
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
    private final JobScheduler jobScheduler;

    MessageSender messageSender;

    RequestModifier requestModifier;
    Logger logger = LoggerFactory.getLogger(JobController.class);

    public JobController(MessageSender messageSender, RequestModifier requestModifier, JobScheduler jobScheduler) {
        this.requestModifier = requestModifier;
        this.messageSender = messageSender;
        this.jobScheduler = jobScheduler;
    }

    @DeleteMapping("/job/stop/{jobUuid}")
    public ResponseEntity stopJob(@PathVariable UUID jobUuid) {
        try {
            this.jobScheduler.stopJob(jobUuid);
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException | InterruptedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/job/schedule")
    public ResponseEntity<UUID> scheduleJob(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobStartTime, @RequestBody RequestFromODSDTO transferRequest) {
        logger.info(transferRequest.toString());
        try {
            UUID id = this.jobScheduler.saveScheduledJob(transferRequest, jobStartTime);
            return ResponseEntity.ok(id);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/job/direct")
    public ResponseEntity<UUID> directJob(@RequestBody TransferJobRequest transferRequest) throws JsonProcessingException, InterruptedException {
        UUID jobUuid = UUID.randomUUID();
        transferRequest.setJobUuid(jobUuid);
        List<EntityInfo> fileList = this.requestModifier.selectAndExpand(transferRequest.getSource(), transferRequest.getSource().getInfoList());
        transferRequest.getSource().setInfoList(fileList);
        this.messageSender.sendMessage(transferRequest, MessageType.TRANSFER_JOB_REQUEST, transferRequest.getTransferNodeName());
        return ResponseEntity.ok(jobUuid);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Collection<TransferJobRequest>> listScheduledJobs(@RequestParam String userEmail) throws JsonProcessingException {
        Collection<TransferJobRequest> futureJobs = jobScheduler.listScheduledJobs(userEmail);
        return ResponseEntity.ok(futureJobs);
    }

    @GetMapping("/job/details")
    public ResponseEntity<TransferJobRequest> getScheduledJob(@RequestParam UUID jobUuid) {
        try {
            TransferJobRequest job = jobScheduler.getScheduledJobDetails(jobUuid);
            return ResponseEntity.ok(job);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/job/delete")
    public ResponseEntity<Void> deleteScheduledJob(@RequestParam UUID jobUuid) {
        jobScheduler.deleteScheduledJob(jobUuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/job/run")
    public ResponseEntity<UUID> runJob(@RequestBody RequestFromODSDTO odsTransferRequest) {
        return this.scheduleJob(LocalDateTime.now(), odsTransferRequest);
    }


}