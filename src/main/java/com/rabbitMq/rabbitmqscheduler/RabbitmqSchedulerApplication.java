package com.rabbitMq.rabbitmqscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class RabbitmqSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqSchedulerApplication.class, args);
	}

}
