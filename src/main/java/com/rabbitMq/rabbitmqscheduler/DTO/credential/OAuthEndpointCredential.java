package com.rabbitMq.rabbitmqscheduler.DTO.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * POJO for storing OAuth Credentials
 */
@Data
@NoArgsConstructor
@Setter
public class OAuthEndpointCredential extends EndpointCredential {
    private String token;
    private boolean tokenExpires = false;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date expiresAt;
    private String refreshToken;
    private boolean refreshTokenExpires = false;

}