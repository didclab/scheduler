package com.rabbitMq.rabbitmqscheduler.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityInfo {
    private String id;
    private String path;
    private long size;
    private int chunkSize;
    private String name;
    private String parent;
    private String checksum;
}
