package com.rabbitMq.rabbitmqscheduler.DTO;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("ods")
public class Properties {

    String AMPQ_PWD;
    String AMPQ_USER;

}
