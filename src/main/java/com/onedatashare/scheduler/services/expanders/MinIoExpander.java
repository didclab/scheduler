package com.onedatashare.scheduler.services.expanders;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.model.credential.EndpointCredential;
import com.onedatashare.scheduler.model.EntityInfo;
import com.onedatashare.scheduler.services.DestinationChunkSize;
import com.onedatashare.scheduler.services.FileExpander;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class MinioExpander extends DestinationChunkSize implements FileExpander {
    private static final Logger logger = LoggerFactory.getLogger(MinioExpander.class);
    private static final int DEFAULT_CHUNK_SIZE = 10000000;
    private static final int MIN_CHUNK_SIZE = 5000000;

    @Getter
    private AmazonS3 minioClient;
    private String[] regionAndBucket;

    @Override
    public void createClient(EndpointCredential cred) {
        try {
            AccountEndpointCredential credential = EndpointCredential.getAccountCredential(cred);
            this.regionAndBucket = credential.getUri().split(":::");
            if (regionAndBucket.length != 2) {
                throw new IllegalArgumentException("Invalid URI format. Expected format: region:::bucket");
            }
            AWSCredentials credentials = new BasicAWSCredentials(
                    credential.getUsername(),
                    credential.getSecret()
            );

            this.minioClient = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(credential.getCustomEndpoint(), regionAndBucket[0]))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withPathStyleAccessEnabled(true)
                    .build();

        } catch (Exception e) {
            logger.error("Failed to create MinIO client", e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }


    @Override
    public List<EntityInfo> expandedFileSystem(List<EntityInfo> userSelectedResources, String basePath) {
        List<EntityInfo> traversedFiles = new LinkedList<>();
        try {
//            System.out.print("check path "+basePath);
            basePath = StringUtils.stripStart(basePath, "/");

            if (userSelectedResources.isEmpty()) {
                ListObjectsV2Result result = minioClient.listObjectsV2(createSkeletonPerResource(basePath));
                traversedFiles.addAll(convertV2ResultToEntityInfoList(result));
                return traversedFiles;
            }

            for (EntityInfo resource : userSelectedResources) {
                if (resource.getPath().endsWith("/")) {
                    ListObjectsV2Request req = createSkeletonPerResource(resource.getPath());
                    ListObjectsV2Result res = minioClient.listObjectsV2(req);
                    for (S3ObjectSummary obj : res.getObjectSummaries()) {
                        if (obj.getKey().endsWith("/")) continue;
                        EntityInfo entityInfo = new EntityInfo();
                        entityInfo.setId(obj.getKey());
                        entityInfo.setPath(obj.getKey());
                        entityInfo.setSize(obj.getSize());
                        traversedFiles.add(entityInfo);
                    }
                } else if (minioClient.doesObjectExist(regionAndBucket[1], resource.getPath())) {
                    ObjectMetadata metadata = minioClient.getObjectMetadata(regionAndBucket[1], resource.getPath());
                    resource.setSize(metadata.getContentLength());
                    traversedFiles.add(resource);
                }
            }
        } catch (Exception e) {
            logger.error("Error expanding file system", e);
            throw new RuntimeException("Failed to expand file system", e);
        }
        return traversedFiles;
    }

    private List<EntityInfo> convertV2ResultToEntityInfoList(ListObjectsV2Result result) {
        List<EntityInfo> traversedFiles = new LinkedList<>();
        for (S3ObjectSummary fileInfo : result.getObjectSummaries()) {
            EntityInfo entityInfo = new EntityInfo();
            entityInfo.setId(fileInfo.getKey());
            entityInfo.setPath(fileInfo.getKey());
            entityInfo.setSize(fileInfo.getSize());
            traversedFiles.add(entityInfo);
        }
        return traversedFiles;
    }

    private ListObjectsV2Request createSkeletonPerResource(String path) {
        return new ListObjectsV2Request()
                .withBucketName(regionAndBucket[1])
                .withPrefix(StringUtils.isEmpty(path) ? null : path);
    }

    @Override
    public List<EntityInfo> destinationChunkSize(List<EntityInfo> expandedFiles, String basePath, Integer userChunkSize) {
        int chunkSize = userChunkSize < MIN_CHUNK_SIZE ? DEFAULT_CHUNK_SIZE : userChunkSize;
        expandedFiles.forEach(file -> file.setChunkSize(chunkSize));
        return expandedFiles;
    }
}
