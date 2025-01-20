package com.onedatashare.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class FileTransferScheduler {

    public static void main(String[] args) {
        SpringApplication.run(FileTransferScheduler.class, args);
    }

}
