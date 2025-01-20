package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.carbon.CarbonIpEntry;
import com.onedatashare.scheduler.model.carbon.CarbonMeasureRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarbonRpcService {

    private Logger log = LoggerFactory.getLogger(CarbonRpcService.class);

    public CarbonRpcService() {
    }

    public List<CarbonIpEntry> traceRoute(CarbonMeasureRequest carbonMeasureRequest) {
        return null;
    }

    public Double averageCarbonIntensityOfTraceRoute(List<CarbonIpEntry> ips) {
        return null;
    }
}
