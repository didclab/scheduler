package com.rabbitMq.rabbitmqscheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is the representaiton of a File/Folder or which is unlikely but a link
 */
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
