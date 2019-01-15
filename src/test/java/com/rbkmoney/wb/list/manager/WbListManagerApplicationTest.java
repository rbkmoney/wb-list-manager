package com.rbkmoney.wb.list.manager;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.rbkmoney.wb.list.manager.config.KafkaConfig;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = WbListManagerApplication.class, initializers = WbListManagerApplicationTest.Initializer.class)
public class WbListManagerApplicationTest extends KafkaAbstractTest {

    public static final String TYPE = "type";
    public static final String BUCKET_NAME = "bucketName";
    public static final String VALUE = "value";
    public static final String KEY = "key";

    @Autowired
    private ListRepository listRepository;
    @Autowired
    private RiakClient client;

    @ClassRule
    public static GenericContainer riak = new GenericContainer("basho/riak-kv")
            .waitingFor(new WaitAllStrategy());

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("riak.port=" + riak.getMappedPort(8087))
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Before
    public void init() throws InterruptedException {
        riak.start();
        // TODO add cycle for up check
        Thread.sleep(8000L);
    }

    @Test
    public void contextLoads() throws ExecutionException, InterruptedException {
        Row row = new Row();
        row.setKey(KEY);
        row.setBucketName(BUCKET_NAME);
        row.setValue(VALUE);
        listRepository.create(row);

        Namespace ns = new Namespace(BUCKET_NAME);
        Location location = new Location(ns, KEY);
        FetchValue fv = new FetchValue.Builder(location).build();
        FetchValue.Response response = client.execute(fv);
        RiakObject obj = response.getValue(RiakObject.class);

        String result = obj.getValue().toString();
        Assert.assertEquals(VALUE, result);

        Optional<Row> resultGet = listRepository.get(BUCKET_NAME, KEY);
        Assert.assertFalse(resultGet.isEmpty());
        Assert.assertEquals(VALUE, resultGet.get().getValue());

        listRepository.remove(row);
        response = client.execute(fv);
        obj = response.getValue(RiakObject.class);
        Assert.assertNull(obj);
    }
}
