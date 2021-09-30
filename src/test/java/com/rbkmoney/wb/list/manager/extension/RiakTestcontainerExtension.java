package com.rbkmoney.wb.list.manager.extension;

import com.rbkmoney.testcontainers.annotations.util.GenericContainerUtil;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class RiakTestcontainerExtension implements BeforeAllCallback, AfterAllCallback {

    private static final ThreadLocal<GenericContainer<?>> THREAD_CONTAINER = new ThreadLocal<>();

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        GenericContainer<?> container = createRiakTestcontainer();
        GenericContainerUtil.startContainer(container);
        THREAD_CONTAINER.set(container);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        var container = THREAD_CONTAINER.get();
        if (container != null && container.isRunning()) {
            container.stop();
        }
        THREAD_CONTAINER.remove();
    }

    private GenericContainer<?> createRiakTestcontainer() {
        try (GenericContainer<?> container = new GenericContainer<>(
                DockerImageName
                        .parse("basho/riak-kv"))
                .withExposedPorts(8087)
                .withNetworkAliases("riak-kv-" + UUID.randomUUID())
                .withEnv("CLUSTER_NAME", "riakkv")
                .withLabel("com.basho.riak.cluster.name", "riak-kv")
                .waitingFor(new WaitAllStrategy()
                        .withStartupTimeout(Duration.ofMinutes(2)))) {
            return container;
        }
    }

    public static class RiakTestcontainerContextCustomizerFactory implements ContextCustomizerFactory {

        @Override
        public ContextCustomizer createContextCustomizer(
                Class<?> testClass,
                List<ContextConfigurationAttributes> configAttributes) {
            return (context, mergedConfig) -> {
                var container = THREAD_CONTAINER.get();
                if (container != null) {
                    TestPropertyValues.of(
                            "riak.address=" + container.getContainerIpAddress(),
                            "riak.port=" + container.getMappedPort(8087))
                            .applyTo(context);
                }
            };
        }
    }
}
