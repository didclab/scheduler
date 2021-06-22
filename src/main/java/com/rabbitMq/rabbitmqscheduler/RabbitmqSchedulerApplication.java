package com.rabbitMq.rabbitmqscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


@SpringBootApplication
@EnableEurekaClient
public class RabbitmqSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqSchedulerApplication.class, args);
	}

}
