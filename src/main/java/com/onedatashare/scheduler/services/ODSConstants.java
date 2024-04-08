package com.onedatashare.scheduler.services;

import com.onedatashare.scheduler.enums.EndPointType;
import com.onedatashare.scheduler.model.credential.AccountEndpointCredential;
import com.onedatashare.scheduler.model.credential.EndpointCredential;

import java.net.URI;

public class ODSConstants {

    public static String uriFromEndpointCredential(EndpointCredential credential, EndPointType type) {
        AccountEndpointCredential ac;
        switch (type) {
            case ftp:
            case sftp:
            case scp:
            case http:
                ac = (AccountEndpointCredential) credential;
                URI uri = URI.create(ac.getUri());
                return uri.getHost();
            case s3:
                ac = (AccountEndpointCredential) credential;
                URI s3Uri = URI.create(constructS3URI(ac.getUri(), ""));
                return s3Uri.getHost();
            case box:
                return "box.com";
            case dropbox:
                return "dropbox.com";
            case gdrive:
                return "drive.google.com";
            default:
                return "";
        }
    }

    public static String constructS3URI(String uri, String fileKey){
        StringBuilder builder = new StringBuilder();
        String[] temp = uri.split(":::");
        String bucketName = temp[1];
        String region = temp[0];
        builder.append("https://").append(bucketName).append(".").append("s3.").append(region).append(".").append("amazonaws.com/").append(fileKey);
        return builder.toString();
    }
}
