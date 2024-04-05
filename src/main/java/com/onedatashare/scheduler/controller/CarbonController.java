package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.RequestFromODS;
import com.onedatashare.scheduler.model.carbon.CarbonIpEntry;
import com.onedatashare.scheduler.model.carbon.CarbonMeasureRequest;
import com.onedatashare.scheduler.model.carbon.CarbonMeasureResponse;
import com.onedatashare.scheduler.model.carbon.CarbonTraceRouteResponse;
import com.onedatashare.scheduler.services.CarbonRpcService;
import com.onedatashare.scheduler.services.JobScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class CarbonController {

    private final CarbonRpcService carbonRpcService;
    private final JobScheduler jobScheduler;

    public CarbonController(CarbonRpcService carbonRpcService, JobScheduler jobScheduler) {
        this.carbonRpcService = carbonRpcService;
        this.jobScheduler = jobScheduler;
    }


    @GetMapping(value = "/measure/carbon/traceroute")
    public ResponseEntity<List<CarbonIpEntry>> traceRouteCarbon(@RequestParam String transferNodeName, @RequestParam String sourceIp, @RequestParam String destinationIp) {
        CarbonMeasureRequest carbonMeasureRequest = new CarbonMeasureRequest(transferNodeName, sourceIp, destinationIp);
        return ResponseEntity.ok(this.carbonRpcService.traceRoute(carbonMeasureRequest));

    }

    @GetMapping(value = "/job/carbon")
    public ResponseEntity<CarbonMeasureResponse> carbonMeasure(@RequestParam UUID jobUuid) {
        Double carbonIntensity = this.jobScheduler.getCarbonIntensityOfJob(jobUuid);
        RequestFromODS job = this.jobScheduler.getScheduledJobDetails(jobUuid);
        CarbonMeasureResponse resp = new CarbonMeasureResponse();
        resp.setAverageCarbonIntensity(carbonIntensity);
        resp.setTransferNodeName(job.getTransferNodeName());
        return ResponseEntity.ok(resp);
    }

    @GetMapping(value = "/job/carbon/traceroute")
    public ResponseEntity<CarbonTraceRouteResponse> carbonTraceRoute(@RequestParam UUID jobUuid) {
        List<CarbonIpEntry> traceRoute = this.jobScheduler.getTraceRoute(jobUuid);
        RequestFromODS job = this.jobScheduler.getScheduledJobDetails(jobUuid);
        CarbonTraceRouteResponse carbonTraceRouteResponse = new CarbonTraceRouteResponse();
        carbonTraceRouteResponse.setTraceRoute(traceRoute);
        carbonTraceRouteResponse.setTransferNodeName(job.getTransferNodeName());
        return ResponseEntity.ok(carbonTraceRouteResponse);
    }
}
