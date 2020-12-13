//package com.rabbitMq.rabbitmqscheduler.Services;
//
//import com.rabbitMq.rabbitmqscheduler.Enums.EndPointType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.io.*;
//
//@Service
//public class CredentialService {
//    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);
//
//
//    public EndpointCredential getCredential(String userId, EndPointType type, String credId) throws IOException {
//        log.info("inside getCredential");
////        StringBuilder result = new StringBuilder();
////        String urlToRead = URI.create(String.format("%s/%s/%s/%s","http://localhost:8081/endpoint-cred", userId, type, credId)).toString();
////        URL url = new URL(urlToRead);
////        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
////        conn.setRequestMethod("GET");
////        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
////        String line;
////        while ((line = rd.readLine()) != null) {
////            result.append(line);
////        }
////        rd.close();
////        ObjectMapper objectMapper = new ObjectMapper();
////        log.info(result.toString());
////        EndpointCredential endpointCredential = objectMapper.readValue(result.toString(),EndpointCredential.class);
////        endpointCredential.setPassWord("pass");
////        log.info("----------------------"+endpointCredential.toString());
////        return endpointCredential;
//
//
//        EndpointCredential endpointCredential = new EndpointCredential();
//        endpointCredential.setAccountId("user");
//        endpointCredential.setPassword("pass");
//        return endpointCredential;
//    }
//
//    public TransferJobRequestWithMetaData addCredentials(TransferJobRequestWithMetaData transferJobRequestWithMetaData)  {
//        log.info("Adding credential");
//        try {
//            EndpointCredential endpointCredential = getCredential(transferJobRequestWithMetaData.getOwnerId(),
//                    transferJobRequestWithMetaData.getSource().getType(), transferJobRequestWithMetaData.getSource().getCredId());
//            transferJobRequestWithMetaData.getSource().setCredential(endpointCredential);
//            transferJobRequestWithMetaData.getDestination().setCredential(endpointCredential);
//            log.info(endpointCredential.toString());
//            return transferJobRequestWithMetaData;
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//            return transferJobRequestWithMetaData;
//        }
//    }
//}
