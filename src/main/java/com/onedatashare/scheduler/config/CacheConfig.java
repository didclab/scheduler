package com.onedatashare.scheduler.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.RestEndpointGroup;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CacheConfig {

    @Value("eureka.client.serviceUrl.defaultZone")
    String eurekaUrl;

    @Bean(name = "hazelcastConfig")
    @Profile("prod")
    public Config prodHazelcastConfig(EurekaClient eurekaClient) {
        Config config = new Config();
        config.getNetworkConfig().getRestApiConfig()
                .setEnabled(true)
                .enableGroups(RestEndpointGroup.DATA);
        config.getNetworkConfig().getJoin().getEurekaConfig()
                .setEnabled(true)
                .setProperty("self-registration", "true")
                .setProperty("namespace", "hazelcast");
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
