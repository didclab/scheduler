package com.rabbitMq.rabbitmqscheduler.Services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.AccountEndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.credential.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Entity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class S3Expander extends DestinationChunkSize implements FileExpander{

    AmazonS3 s3Client;
    String[] regionAndBucket;

    @Override
    public void createClient(EndpointCredential cred) {
        AccountEndpointCredential credential = EndpointCredential.getAccountCredential(cred);
        this.regionAndBucket = credential.getUri().split(":::");
        AWSCredentials credentials = new BasicAWSCredentials(credential.getUsername(), credential.getSecret());
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(regionAndBucket[0])
                .build();
    }

    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        List<EntityInfo> traversedFiles = new LinkedList<>();
        if(userSelectedResources.isEmpty()){//expand the whole bucket relative to the basePath
            ListObjectsV2Result result = this.s3Client.listObjectsV2(createSkeletonPerResource(basePath));
            traversedFiles.addAll(convertV2ResultToEntityInfoList(result));
        }else{
            for(EntityInfo userSelectedResource: userSelectedResources){
                String path = basePath+userSelectedResource.getPath();
                ListObjectsV2Result result = this.s3Client.listObjectsV2(createSkeletonPerResource(path));
                traversedFiles.addAll(convertV2ResultToEntityInfoList(result));
            }
        }
        return traversedFiles;
    }

    public List<EntityInfo> convertV2ResultToEntityInfoList(ListObjectsV2Result result){
        List<EntityInfo> traversedFiles = new LinkedList<>();
        for(S3ObjectSummary fileInfo : result.getObjectSummaries()){
            EntityInfo entityInfo = new EntityInfo();
            entityInfo.setId(fileInfo.getKey());
            entityInfo.setPath(fileInfo.getKey());
            entityInfo.setSize(fileInfo.getSize());
            traversedFiles.add(entityInfo);
        }
        return traversedFiles;
    }

    public ListObjectsV2Request createSkeletonPerResource(String path){
        if(path.isEmpty()){
            return new ListObjectsV2Request()
                    .withBucketName(regionAndBucket[1]);
        }else{
            return new ListObjectsV2Request()
                    .withBucketName(regionAndBucket[1])
                    .withPrefix(path);
        }
    }

    @Override
    public List<EntityInfo> destinationChunkSize(List<EntityInfo> expandedFiles, String basePath, Integer userChunkSize) {
        for (EntityInfo fileInfo : expandedFiles) {
            if(userChunkSize < 5000000){
                fileInfo.setChunkSize(10000000);
            }else{
                fileInfo.setChunkSize(userChunkSize);
            }
        }
        return expandedFiles;
    }
}
