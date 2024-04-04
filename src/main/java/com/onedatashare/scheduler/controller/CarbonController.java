package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.CarbonMeasureRequest;
import com.onedatashare.scheduler.model.CarbonMeasureResponse;
import com.onedatashare.scheduler.services.CarbonRpcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CarbonController {

    private final CarbonRpcService carbonRpcService;

    public CarbonController(CarbonRpcService carbonRpcService) {
        this.carbonRpcService = carbonRpcService;
    }

    @GetMapping(value = "/measure/carbon/path")
    public ResponseEntity<Object> measureCarbonNetworkPath(@RequestParam String transferNodeName, @RequestParam String sourceIp, @RequestParam String destinationIp) {
        CarbonMeasureRequest carbonMeasureRequest = new CarbonMeasureRequest(transferNodeName, sourceIp, destinationIp);
        CarbonMeasureResponse carbonMeasureResponse = this.carbonRpcService.measureCarbon(carbonMeasureRequest);
        return ResponseEntity.ok(carbonMeasureResponse);
    }

    @GetMapping(value = "/measure/carbon/traceroute")
    public ResponseEntity<Map<String, Object>> traceRouteCarbon(@RequestParam String transferNodeName, @RequestParam String sourceIp, @RequestParam String destinationIp) {
        CarbonMeasureRequest carbonMeasureRequest = new CarbonMeasureRequest(transferNodeName, sourceIp, destinationIp);
        return ResponseEntity.ok(this.carbonRpcService.traceRoute(carbonMeasureRequest));

    }

}
