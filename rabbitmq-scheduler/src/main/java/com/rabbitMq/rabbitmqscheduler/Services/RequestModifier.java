package com.rabbitMq.rabbitmqscheduler.Services;

import com.rabbitMq.rabbitmqscheduler.DTO.RequestFromODS;
import com.rabbitMq.rabbitmqscheduler.DTO.TransferJobRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestModifier {

//    public EndpointCredential getCredential(String userId, EndPointType type, String credId) throws IOException {
//        log.info("inside getCredential");
//        StringBuilder result = new StringBuilder();
//        String urlToRead = URI.create(String.format("%s/%s/%s/%s","http://localhost:8081/endpoint-cred", userId, type, credId)).toString();
//        URL url = new URL(urlToRead);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        String line;
//        while ((line = rd.readLine()) != null) {
//            result.append(line);
//        }
//        rd.close();
//        ObjectMapper objectMapper = new ObjectMapper();
//        log.info(result.toString());
//        EndpointCredential endpointCredential = objectMapper.readValue(result.toString(),EndpointCredential.class);
//        endpointCredential.setPassWord("pass");
//        log.info("----------------------"+endpointCredential.toString());
//        return endpointCredential;
//
//
//        EndpointCredential endpointCredential = new EndpointCredential();
//        endpointCredential.setAccountId("user");
//        endpointCredential.setPassword("pass");
//        return endpointCredential;
//    }

    public TransferJobRequest createRequest(RequestFromODS odsTransferRequest) {
        TransferJobRequest transferJobRequest = new TransferJobRequest("100", "vsingh27@buffalo.edu", 1, 64000);
//        TransferJobRequest transferJobRequest = getCredential();

        return transferJobRequest;
    }
}
