package com.onedatashare.scheduler.model.carbon;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CarbonMeasurement {
    List<CarbonIpEntry> traceRouteCarbon;
    String ownerId;
    String transferNodeName;
    String jobUuid;
    LocalDateTime timeMeasuredAt;
}
