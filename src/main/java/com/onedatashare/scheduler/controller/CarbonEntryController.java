package com.onedatashare.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onedatashare.scheduler.model.InitialAndFinal;
import com.onedatashare.scheduler.model.carbon.CarbonMeasurement;
import com.onedatashare.scheduler.services.maps.CarbonMapService;
import com.onedatashare.scheduler.services.maps.InitialAndFinalJobCarbonMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carbon")
public class CarbonEntryController {

    private final CarbonMapService carbonMapService;
    private final InitialAndFinalJobCarbonMapService initialAndFinalCarbonMap;


    public CarbonEntryController(CarbonMapService carbonMapService, InitialAndFinalJobCarbonMapService initialAndFinalJobCarbonMapService) {
        this.carbonMapService = carbonMapService;
        this.initialAndFinalCarbonMap = initialAndFinalJobCarbonMapService;
    }

    /***
     * Returns the most recent carbon information for the scheduled job.
     * @param uuid: the job UUID
     * @return: a list of the carbon intensity
     */
    @GetMapping("/latest/{uuid}")
    public ResponseEntity<CarbonMeasurement> getLatestCarbonIntensityForJob(@PathVariable String uuid) {
        try {
            return ResponseEntity.ok(this.carbonMapService.getLatestCarbonEntryForJob(UUID.fromString(uuid)));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /***
     * Returns every measurment of the scheduled job.
     * @param uuid: the job UUIID
     * @return
     */
    @GetMapping("/all/{uuid}")
    public ResponseEntity<List<CarbonMeasurement>> getAllCarbonIntensityForJob(@PathVariable String uuid) {
        try {
            return ResponseEntity.ok(this.carbonMapService.getAllCarbonIntensityForJob(UUID.fromString(uuid)));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/entry")
    public ResponseEntity<List<CarbonMeasurement>> getUserAndTransferNodeCarbonEntriesForJob(@RequestParam("jobUuid") UUID jobUuid, @RequestParam("transferNodeName") String transferNodeName, @RequestParam("userEmail") String userEmail) {
        try {
            return ResponseEntity.ok(this.carbonMapService.getUserAndTransferNodeCarbonEntriesForJob(jobUuid, transferNodeName, userEmail));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<CarbonMeasurement>> getAllUserCarbonEntries(@RequestParam("userEmail") String userEmail) {
        try {
            return ResponseEntity.ok(this.carbonMapService.getAllUserEntries(userEmail));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/node/{transferNodeName}")
    public ResponseEntity<List<CarbonMeasurement>> getAllNodeEntries(@PathVariable String transferNodeName) {
        try {
            return ResponseEntity.ok(this.carbonMapService.getNodeCarbonMeasurements(transferNodeName));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/job/result/{uuid}")
    public ResponseEntity<InitialAndFinal<CarbonMeasurement>> jobCarbonResult(@PathVariable UUID uuid) {
        return ResponseEntity.ok(this.initialAndFinalCarbonMap.getInitialAndFinal(uuid));
    }

}
