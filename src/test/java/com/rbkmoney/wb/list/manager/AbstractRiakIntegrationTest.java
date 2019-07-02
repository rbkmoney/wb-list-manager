package com.rbkmoney.wb.list.manager;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.time.Duration;


@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = AbstractRiakIntegrationTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractRiakIntegrationTest extends KafkaAbstractTest{

    @ClassRule
    public static GenericContainer riak = new GenericContainer("basho/riak-kv")
            .withExposedPorts(8098, 8087)
            .withPrivilegedMode(true)
            .waitingFor(new WaitAllStrategy()
                    .withStartupTimeout(Duration.ofMinutes(2)));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("riak.port=" + riak.getMappedPort(8087))
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
