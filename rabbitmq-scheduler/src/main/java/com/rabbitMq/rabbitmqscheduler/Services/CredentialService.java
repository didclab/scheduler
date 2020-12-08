package com.rabbitMq.rabbitmqscheduler.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitMq.rabbitmqscheduler.DTO.EndpointCredential;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequestTransferNode;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequestWithMetaData;
import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;

@Service
public class CredentialService {

    public EndpointCredential getCredential(String userId, EndPointType type, String credId) throws IOException {
        StringBuilder result = new StringBuilder();
        String urlToRead = URI.create(String.format("%s/%s/%s/%s","http://localhost:8081/endpoint-cred", userId, type, credId)).toString();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        ObjectMapper objectMapper = new ObjectMapper();
        EndpointCredential endpointCredential = objectMapper.readValue(result.toString(),EndpointCredential.class);
        return endpointCredential;
    }

    public TransferJobRequestWithMetaData addCredentials(TransferJobRequestWithMetaData transferJobRequestWithMetaData)  {
        try {
            EndpointCredential endpointCredential = getCredential(transferJobRequestWithMetaData.getOwnerId(),
                    transferJobRequestWithMetaData.getSource().getType(), transferJobRequestWithMetaData.getSource().getCredId());
            transferJobRequestWithMetaData.getSource().setCredential(endpointCredential);
            transferJobRequestWithMetaData.getDestination().setCredential(endpointCredential);
            return transferJobRequestWithMetaData;
        }
        catch (Exception ex){
            return transferJobRequestWithMetaData;
        }
    }
}
