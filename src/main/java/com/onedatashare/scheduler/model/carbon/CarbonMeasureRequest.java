package com.onedatashare.scheduler.model.carbon;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CarbonMeasureRequest {

    public String transferNodeName;
    public String sourceIp;
    public String destinationIp;
}
