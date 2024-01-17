package com.onedatashare.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptimizationParameterSet {
    public int concurrency;
    public int parallelism;
    public int pipelining;
}
