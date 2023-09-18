package com.rabbitMq.rabbitmqscheduler.controller;

import com.rabbitMq.rabbitmqscheduler.model.RequestFromODSDTO;
import com.rabbitMq.rabbitmqscheduler.model.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.model.TransferJobResponse;
import com.rabbitMq.rabbitmqscheduler.services.JobScheduler;
import com.rabbitMq.rabbitmqscheduler.services.MessageSender;
import com.rabbitMq.rabbitmqscheduler.services.RequestModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@RestController
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    private final JobScheduler jobScheduler;

    @Autowired
    MessageSender messageSender;

    @Autowired
    RequestModifier requestModifier;

    public JobController(MessageSender messageSender, RequestModifier requestModifier, JobScheduler jobScheduler) {
        this.requestModifier = requestModifier;
        this.messageSender = messageSender;
        this.jobScheduler = jobScheduler;
    }

    @PostMapping("/job/schedule")
    public ResponseEntity<Boolean> scheduleJob(@RequestParam Instant jobStartTime, @RequestBody RequestFromODSDTO transferRequest) {
        boolean status = this.jobScheduler.saveScheduledJob(transferRequest, jobStartTime);
        if (status) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.badRequest().body(status);
        }
    }

    @GetMapping("/job")
    public ResponseEntity<Collection<RequestFromODSDTO>> listScheduledJobs(@RequestParam String userEmail) {
        Collection<RequestFromODSDTO> futureJobs = jobScheduler.listScheduledJobs(userEmail);
        return ResponseEntity.ok(futureJobs);
    }

    @GetMapping("/job/details")
    public ResponseEntity<RequestFromODSDTO> getScheduledJob(@RequestParam UUID jobUuid) {
        RequestFromODSDTO job = jobScheduler.getScheduledJobDetails(jobUuid);
        if (job == null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(job);
        }
    }

    @DeleteMapping("/job/delete")
    public ResponseEntity<Void> deleteScheduledJob(@RequestParam UUID jobUuid) {
        jobScheduler.deleteScheduledJob(jobUuid);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/job/run")
    public ResponseEntity<TransferJobResponse> runJob(@RequestBody RequestFromODSDTO odsTransferRequest) {
        TransferJobRequest transferJobRequest = requestModifier.createRequest(odsTransferRequest);
        logger.info(transferJobRequest.toString());
        messageSender.sendTransferRequest(transferJobRequest);
        TransferJobResponse response = new TransferJobResponse();
        response.setMessage("Job Submitted");
        return ResponseEntity.ok(response);
    }
}