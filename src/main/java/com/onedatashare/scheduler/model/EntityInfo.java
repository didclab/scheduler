package com.onedatashare.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * This is the representaiton of a File/Folder or which is unlikely but a link
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityInfo implements Serializable {
    private String id;
    private String path;
    private long size;
    private int chunkSize;
    private String name;
    private String parent;
    private String checksum;
}
