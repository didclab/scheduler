package com.onedatashare.scheduler.services;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.model.CarbonMeasureRequest;
import com.onedatashare.scheduler.model.CarbonMeasureResponse;
import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.RequestFromODSDTO;
import com.onedatashare.scheduler.model.credential.EndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JobScheduler {

    protected IMap<UUID, RequestFromODS> jobIMap;
    public IMap<UUID, CarbonMeasureResponse> carbonJobMap; //scheduling in time is abusing this map.
    public IMap<UUID, CarbonMeasureResponse> initialCarbonMeasurement;

    public IMap<UUID, Double> userDesiredCarbonIntensity;
    private EntryExpiredHazelcast entryExpiredHazelcast;
    Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    @Value("${ods.rabbitmq.routingkey}")
    private String routingKey;

    @Autowired
    CarbonRpcService carbonRpcService;

    @Autowired
    CredentialService credentialService;


    public JobScheduler(HazelcastInstance hazelcastInstance, RequestModifier requestModifier, MessageSender messageSender) {
        this.jobIMap = hazelcastInstance.getMap("scheduled_jobs");
        this.carbonJobMap = hazelcastInstance.getMap("carbon_map");
        this.userDesiredCarbonIntensity = hazelcastInstance.getMap("user_desired_carbon_intensity");
        this.entryExpiredHazelcast = new EntryExpiredHazelcast(requestModifier, messageSender);
    }

    public Collection<RequestFromODS> listScheduledJobs(String userEmail) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        Predicate<UUID, RequestFromODS> predicate = entry -> entry.getValue().getOwnerId().equals(userEmail);
        return this.jobIMap.values(predicate);
    }

    public RequestFromODS getScheduledJobDetails(UUID jobUuid) {
        return this.jobIMap.get(jobUuid);
    }

    public boolean deleteScheduledJob(UUID jobUuid) {
        if (this.jobIMap.containsKey(jobUuid)) {
            this.jobIMap.delete(jobUuid);
            return true;
        } else {
            return false;
        }
    }

    public UUID saveScheduledJob(RequestFromODSDTO transferRequest, LocalDateTime jobStartTime) {
        if (transferRequest == null) {
            return null;
        }
        UUID id = UUID.randomUUID();
        Instant currentDate = Instant.now();
        RequestFromODS transferJob = new RequestFromODS();
        transferJob.setOptions(transferRequest.getOptions());
        transferJob.setDestination(transferRequest.getDestination());
        transferJob.setSource(transferRequest.getSource());
        transferJob.setOwnerId(transferRequest.getOwnerId());
        transferJob.setJobUuid(id);
        transferJob.setJobStartTime(jobStartTime);
        boolean sourceVfs = transferRequest.getSource().getType().equals(EndPointType.vfs);
        boolean destVfs = transferRequest.getDestination().getType().equals(EndPointType.vfs);
        if (transferRequest.getTransferNodeName() != null || !transferRequest.getTransferNodeName().isEmpty()) {
            transferJob.setTransferNodeName(transferRequest.getTransferNodeName());
        } else if (sourceVfs) {
            transferJob.setTransferNodeName(transferRequest.getSource().getCredId());
        } else if (destVfs) {
            transferJob.setTransferNodeName(transferRequest.getDestination().getCredId());
        } else {
            transferJob.setTransferNodeName(routingKey);
        }
        long delay = Duration.between(LocalDateTime.now(), jobStartTime).getSeconds();
        logger.info("Now: {} \t jobStartTime: {} \t delay: {}", currentDate, jobStartTime, delay);
        if (delay < 0) delay = 1;
        this.jobIMap.put(id, transferJob, delay, TimeUnit.SECONDS);
        this.jobIMap.addEntryListener(this.entryExpiredHazelcast, id, true);
        this.measureCarbonForJob(transferJob, id);
        //schedule around carbon intensity
        if(transferRequest.getOptions().getUserDesiredCarbonIntensity() > -1){

        }
        return id;
    }

    @Scheduled(cron = "* */30 * * *")
    public void rescheduleJob() {
        this.jobIMap.keySet().parallelStream()
                .forEach(jobUuid -> {

                });

    }

    public void measureCarbonForJob(RequestFromODS requestFromODS, UUID jobUuid){
        EndpointCredential sourceCred;
        EndpointCredential destCred;
        if (RequestModifier.vfsCredType.contains(requestFromODS.getSource().getType().toString())) {
            sourceCred = credentialService.fetchAccountCredential(requestFromODS.getSource().getType().toString(), requestFromODS.getOwnerId(), requestFromODS.getSource().getCredId());
        } else {
            sourceCred = credentialService.fetchOAuthCredential(requestFromODS.getSource().getType(), requestFromODS.getOwnerId(), requestFromODS.getSource().getCredId());
        }
        if (RequestModifier.vfsCredType.contains(requestFromODS.getDestination().getType().toString())) {
            destCred = credentialService.fetchAccountCredential(requestFromODS.getDestination().getType().toString(), requestFromODS.getOwnerId(), requestFromODS.getDestination().getCredId());
        } else {
            destCred = credentialService.fetchOAuthCredential(requestFromODS.getDestination().getType(), requestFromODS.getOwnerId(), requestFromODS.getDestination().getCredId());
        }
        String sourceUri = ODSConstants.uriFromEndpointCredential(sourceCred, requestFromODS.getSource().getType());
        String destUri = ODSConstants.uriFromEndpointCredential(destCred, requestFromODS.getDestination().getType());
        CarbonMeasureResponse carbonMeasureResponse = this.carbonRpcService.measureCarbon(new CarbonMeasureRequest(requestFromODS.getTransferNodeName(), sourceUri, destUri));
        this.initialCarbonMeasurement.put(jobUuid, carbonMeasureResponse);
    }
}
