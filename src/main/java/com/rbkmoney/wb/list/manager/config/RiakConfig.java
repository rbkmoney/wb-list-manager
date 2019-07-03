package com.rbkmoney.wb.list.manager.config;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.netty.PingHealthCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RiakConfig {

    @Value("${riak.port}")
    public int riakPort;

    @Value("${riak.address}")
    public String riakAddress;

    @Bean
    public RiakCluster riakCluster() {
        RiakNode node = new RiakNode.Builder()
                .withRemoteAddress(riakAddress)
                .withRemotePort(riakPort)
                .build();
        RiakCluster cluster = new RiakCluster.Builder(node)
                .build();
        cluster.start();
        return cluster;
    }

    @Bean
    public RiakClient riakClient(RiakCluster riakCluster) {
        return new RiakClient(riakCluster);
    }
}
