package com.onedatashare.scheduler.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.DataConnectionConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.eureka.one.EurekaOneDiscoveryStrategyFactory;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;

@Configuration
public class CacheConfig {


    @Bean(name = "hazelcastConfig")
    @Profile("prod")
    public Config prodHazelcastConfig(EurekaClient eurekaClient) {
        Config config = new Config();
        config.setClusterName("Transfer-Scheduler-Cluster");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        EurekaOneDiscoveryStrategyFactory.setEurekaClient(eurekaClient);
        config.getNetworkConfig().getJoin().getEurekaConfig().setEnabled(true)
                .setProperty("namespace", "hazelcast")
                .setProperty("use-classpath-eureka-client-props", "false")
                .setProperty("shouldUseDns", "false")
                .setProperty("self-registration", "true")
                .setProperty("use-metadata-for-host-and-port", "true");

        return config;
    }

    @Bean(name = "hazelcastConfig")
//    @Profile("dev")
    public Config devHazelcastConfig() {
        Config config = new Config();
        config.setClusterName("scheduler-cluster");
        config.getNetworkConfig().setPortAutoIncrement(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getJetConfig().setEnabled(true);
        config.addDataConnectionConfig(
                        new DataConnectionConfig("cockroach-db")
                                .setType("JDBC")
                                .setProperty("jdbcUrl", "jdbc:postgresql://flashy-crow-1605.jxf.gcp-us-east1.cockroachlabs.cloud:26257/ods?sslmode=verify-full")
                                .setProperty("user", "ritika")
                                .setProperty("password", "qFojunYYoUHuvvWnjp5TOQ")
                                .setShared(true)
                );
        config.addMapConfig(getMapConfigForMapLoader());
        return config;
    }

    private MapConfig getMapConfigForMapLoader() {
        MapConfig mapConfig = new MapConfig("scheduled_jobs");
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setClassName("com.hazelcast.mapstore.GenericMapStore");
        mapStoreConfig.setProperty("data-connection-ref", "cockroach-db");
        mapStoreConfig.setProperty("external-name", "scheduled_jobs");
        mapStoreConfig.setProperty("mapping-type", "JDBC");
        mapStoreConfig.setProperty("id-column", "uuid");
        mapStoreConfig.setProperty("columns", "jobdetail");
        mapConfig.setMapStoreConfig(mapStoreConfig);
        return mapConfig;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return HazelcastInstanceFactory.newHazelcastInstance(hazelcastConfig);
    }

    @Bean
    public TransportClientFactories transportClientFactories() {
        return Jersey3TransportClientFactories.getInstance();
    }

}
