package com.onedatashare.scheduler.controller;

import com.onedatashare.scheduler.model.FileTransferNodeMetaData;
import com.onedatashare.scheduler.services.FileTransferNodeDiscovery;
import jakarta.ws.rs.QueryParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class FileTransferNodeController {


    private final FileTransferNodeDiscovery fileTransferNodeDiscovery;

    public FileTransferNodeController(FileTransferNodeDiscovery fileTransferNodeDiscovery) {
        this.fileTransferNodeDiscovery = fileTransferNodeDiscovery;
    }

    @GetMapping("/connectors")
    public ResponseEntity<List<FileTransferNodeMetaData>> getUserOdsConnectors(@QueryParam("user") String user) {
        return ResponseEntity.ok(this.fileTransferNodeDiscovery.getUsersFileTransferNodes(user));
    }

    @GetMapping("/ods")
    public ResponseEntity<List<FileTransferNodeMetaData>> getOdsNodes() {
        return ResponseEntity.ok(this.fileTransferNodeDiscovery.getOdsNodes());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> totalConnectedFileTransferNodes() {
        return ResponseEntity.ok(this.fileTransferNodeDiscovery.totalConnectedFileTransferNodes());
    }
}
