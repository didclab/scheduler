package com.rabbitMq.rabbitmqscheduler.Services.expanders;

import com.jcraft.jsch.*;
import com.rabbitMq.rabbitmqscheduler.DTO.EntityInfo;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

@Service
public class SFTPExpander {

    Logger logger = LoggerFactory.getLogger(SFTPExpander.class);

    Session jschSession = null;

    public ArrayList<EntityInfo> expandSftpPath(TransferJobRequest.Source source) {
        ArrayList<EntityInfo> filesToTransferList = new ArrayList<>();
        JSch jsch = new JSch();
        try {
//            jsch.addIdentity("/home/vishal/.ssh/ods-bastion-dev.pem");
//            jsch.setKnownHosts("/home/vishal/.ssh/known_hosts");
            jsch.addIdentity("randomName", source.getVfsSourceCredential().getSecret().getBytes(), null, null);
            jschSession = jsch.getSession(source.getVfsSourceCredential().getUsername(), source.getVfsSourceCredential().getUri().split(":")[0]);
            jschSession.setConfig("StrictHostKeyChecking", "no");
            jschSession.connect();
            jschSession.setTimeout(10000);
            Channel sftp = jschSession.openChannel("sftp");
            ChannelSftp channelSftp = (ChannelSftp) sftp;
            channelSftp.connect();
            System.out.println("Current Path : " + channelSftp.pwd());
            String parentInfoPath = source.getParentInfo().getPath();
            List<String> paths = new ArrayList<String>();
            for (EntityInfo e : source.getInfoList()) {
                paths.add(e.getPath());
            }
            Stack<String> dirPath = new Stack<String>();
            dirPath.add(parentInfoPath);
            boolean firstRound = true;
            while (!dirPath.isEmpty()) {
                channelSftp.cd(dirPath.pop() + "/");
                System.out.println("Current Path after cd : " + channelSftp.pwd());
                Vector<ChannelSftp.LsEntry> list = channelSftp.ls(".");
                for (ChannelSftp.LsEntry entry : list) {
                    if (entry.getFilename().equals(".") || entry.getFilename().equals(".."))
                        continue;
                    if (paths.contains(entry.getFilename()) || !firstRound) {
                        if (entry.getAttrs().isDir()) {
                            dirPath.add(channelSftp.pwd() + "/" + entry.getFilename());
                        } else {
                            EntityInfo ei = new EntityInfo();
                            ei.setPath(channelSftp.pwd() + "/" + entry.getFilename());
                            ei.setSize(entry.getAttrs().getSize());
                            filesToTransferList.add(ei);
                        }
                    }
                }
                firstRound = false;
            }
            System.out.println(filesToTransferList.toString());

        } catch (JSchException e) {
            logger.error("Error in JSch end");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filesToTransferList;
    }

}