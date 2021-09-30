package com.rbkmoney.wb.list.manager.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.time.Duration;
import java.util.UUID;

public class RiakContainerExtension implements BeforeAllCallback, AfterAllCallback {

    private GenericContainer riak;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        riak = new GenericContainer("basho/riak-kv")
                .withExposedPorts(8098, 8087)
                .withPrivilegedMode(true)
                .withNetworkAliases("riak-kv-" + UUID.randomUUID())
                .withEnv("CLUSTER_NAME", "riakts")
                .withEnv("WAIT_FOR_ERLANG", "1000")
                .withLabel("com.basho.riak.cluster.name", "riakts")
                .waitingFor(new WaitAllStrategy()
                        .withStartupTimeout(Duration.ofMinutes(2)));

        riak.start();
        System.setProperty("riak.port", String.valueOf(riak.getMappedPort(8087)));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        riak.stop();
    }
}
