package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.MessageType;
import com.onedatashare.scheduler.model.TransferJobRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransferNodeService {

    private final ODSRouter odsRouter;

    public TransferNodeService(ODSRouter odsRouter) {
        this.odsRouter = odsRouter;
    }

    public List<String> listUserTransferNodes(String userName) {
        Map<String, Map<String, Boolean>> userNodeMap = this.odsRouter.getUserNodeMap();
        Map<String, Boolean> nodes = userNodeMap.get(userName);
        if (nodes != null) {
            return nodes.keySet().stream().toList();
        }
        return new ArrayList<>();
    }

    public List<TransferJobRequest> listUserJobs(String userName) {
        Map<String, Map<String, Boolean>> userNodeMap = this.odsRouter.getUserNodeMap();
        Map<String, Boolean> nodes = userNodeMap.get(userName);
        if (nodes != null) {
            nodes.keySet()
                    .stream()
                    .forEach(nodeName -> {
                        this.odsRouter.sendPojo("", nodeName,MessageType.LIST_JOBS);
                    });
        }
        return new ArrayList<>();
    }
}
