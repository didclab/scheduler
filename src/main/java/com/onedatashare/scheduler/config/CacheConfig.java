package com.onedatashare.scheduler.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CacheConfig {

    @Value("${eureka.client.serviceUrl.defaultZone}")
    String eurekaUrl;

    @Value("${spring.application.name}")
    String springName;

    @Bean(name = "hazelcastConfig")
    @Profile("prod")
    public Config prodHazelcastConfig() {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true)
                .setProperty("HZ_DISCOVERY", System.getenv("HZ_DISCOVERY"))
                .setProperty("HZ_DISCOVERY_SECRET", System.getenv("HZ_DISCOVERY_SECRET"))
                .setProperty("use-public-ip", "true");
        return config;
    }

    @Bean(name = "hazelcastConfig")
    @Profile("dev")
    public Config devHazelcastConfig() {
        Config config = new Config();
        config.setClusterName("scheduler-cluster");
        config.getNetworkConfig().setPortAutoIncrement(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return HazelcastInstanceFactory.newHazelcastInstance(hazelcastConfig);
    }

}
