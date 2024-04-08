package com.onedatashare.scheduler.model.carbon;

import lombok.Data;

import java.util.List;

@Data
public class CarbonTraceRouteResponse {
    List<CarbonIpEntry> traceRoute;
    String transferNodeName;
}
