package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;

import java.util.List;

public abstract class DestinationChunkSize {

    /**
     * A class should override this if that protocol needs to get the chunkSize determined by Destination
     * @param expandedFiles
     * @param basePath
     * @return
     */
    public List<EntityInfo> destinationChunkSize(List<EntityInfo> expandedFiles, String basePath, Integer userChunkSize){
        for(EntityInfo fileInfo : expandedFiles){
            fileInfo.setChunkSize(userChunkSize);
        }
        return expandedFiles;
    }

}
