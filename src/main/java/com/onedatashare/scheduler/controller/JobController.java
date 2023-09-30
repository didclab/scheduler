package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.services.JobScheduler;
import com.onedatashare.scheduler.services.MessageSender;
import com.onedatashare.scheduler.services.RequestModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<UUID> scheduleJob(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobStartTime, @RequestBody RequestFromODSDTO transferRequest) {
        UUID id = this.jobScheduler.saveScheduledJob(transferRequest, jobStartTime);
        if (id == null) {
            return ResponseEntity.badRequest().body(null);
        } else {
            return ResponseEntity.ok(id);
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<Collection<RequestFromODS>> listScheduledJobs(@RequestParam String userEmail) {
        Collection<RequestFromODS> futureJobs = jobScheduler.listScheduledJobs(userEmail);
        return ResponseEntity.ok(futureJobs);
    }

    @GetMapping("/job/details")
    public ResponseEntity<RequestFromODS> getScheduledJob(@RequestParam UUID jobUuid) {
        RequestFromODS job = jobScheduler.getScheduledJobDetails(jobUuid);
        if (job == null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(job);
        }
    }

    @DeleteMapping("/job/delete")
    public ResponseEntity<Void> deleteScheduledJob(@RequestParam UUID jobUuid) {
        boolean status = jobScheduler.deleteScheduledJob(jobUuid);
        if (status) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/job/run")
    public ResponseEntity<UUID> runJob(@RequestBody RequestFromODSDTO odsTransferRequest) {
        return this.scheduleJob(LocalDateTime.now(), odsTransferRequest);
    }
}