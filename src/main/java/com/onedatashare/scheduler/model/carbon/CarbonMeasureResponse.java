package com.onedatashare.scheduler.model.carbon;

import lombok.Data;

@Data
public class CarbonMeasureResponse {
    public String transferNodeName;
    public Double averageCarbonIntensity;
}
