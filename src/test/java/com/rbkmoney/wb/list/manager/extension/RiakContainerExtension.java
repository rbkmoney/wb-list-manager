package com.rbkmoney.wb.list.manager.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.time.Duration;

public class RiakContainerExtension implements BeforeAllCallback {

    public static GenericContainer RIAK;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        RIAK = new GenericContainer("basho/riak-kv")
                .withExposedPorts(8098, 8087)
                .withPrivilegedMode(true)
                .waitingFor(new WaitAllStrategy()
                        .withStartupTimeout(Duration.ofMinutes(2)));

        RIAK.start();
    }
}
