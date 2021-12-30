package com.rabbitMq.rabbitmqscheduler.DTO;

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
}
