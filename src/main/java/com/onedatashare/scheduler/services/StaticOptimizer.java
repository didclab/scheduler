package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.model.OptimizationParameterSet;
import com.onedatashare.scheduler.model.TransferOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class StaticOptimizer {

    private HashMap<Integer, OptimizationParameterSet> parameterBucket;

    public StaticOptimizer() {
        this.parameterBucket = this.parameterBucket();
    }

    public HashMap<Integer, OptimizationParameterSet> parameterBucket() {
        HashMap<Integer, OptimizationParameterSet> map = new HashMap<>();
        OptimizationParameterSet smallFiles = new OptimizationParameterSet(8, 1, 16);
        OptimizationParameterSet medium = new OptimizationParameterSet(4, 2, 8);
        OptimizationParameterSet large = new OptimizationParameterSet(8, 2, 4);
        OptimizationParameterSet largest = new OptimizationParameterSet(5, 3, 3);
        map.put(0, smallFiles);
        map.put(1, medium);
        map.put(2, large);
        map.put(3, largest);
        return map;
    }

    public TransferOptions decideParams(List<EntityInfo> files, TransferOptions transferOptions) {
        double averageMb = files
                .stream()
                .filter(entityInfo -> entityInfo.getSize() > 0)
                .mapToDouble(entityInfo -> entityInfo.getSize() / (1024.0 * 1024.0)) // Convert bytes to MB
                .average().orElse(0.0);
        OptimizationParameterSet paramsToUse = new OptimizationParameterSet(transferOptions.getConcurrencyThreadCount(), transferOptions.getParallelThreadCount(), transferOptions.getPipeSize());
        if (averageMb >= .25 && averageMb <= 25) {
            paramsToUse = this.parameterBucket.get(0);
        } else if (averageMb >= 26 && averageMb <= 100) {
            paramsToUse = this.parameterBucket.get(1);
        } else if (averageMb >= 101 && averageMb <= 600) {
            paramsToUse = this.parameterBucket.get(2);
        } else if (averageMb >= 600) {
            paramsToUse = this.parameterBucket.get(3);
        }
        transferOptions.setConcurrencyThreadCount(paramsToUse.getConcurrency());
        transferOptions.setParallelThreadCount(paramsToUse.getParallelism());
        transferOptions.setPipeSize(paramsToUse.getPipelining());
        return transferOptions;
    }

}
