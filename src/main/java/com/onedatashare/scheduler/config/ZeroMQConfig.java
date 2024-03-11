package com.onedatashare.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zeromq.ZContext;

@Configuration
public class ZeroMQConfig {

    @Bean(destroyMethod = "close")
    public ZContext zContext() {
        return new ZContext();
    }

}
