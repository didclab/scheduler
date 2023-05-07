package com.rabbitMq.rabbitmqscheduler;

import com.rabbitMq.rabbitmqscheduler.DTO.Properties;
import com.rabbitmq.client.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


@SpringBootApplication
@EnableEurekaClient
@EnableConfigurationProperties({Properties.class})
public class RabbitmqSchedulerApplication  {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqSchedulerApplication.class, args);

	}

}
