package com.rbkmoney.wb.list.manager;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.rbkmoney.damsel.wb_list.*;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import com.rbkmoney.wb.list.manager.serializer.EventDeserializer;
import com.rbkmoney.wb.list.manager.utils.ChangeCommandWrapper;
import com.rbkmoney.woody.thrift.impl.http.THClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = WbListManagerApplication.class, initializers = WbListManagerApplicationTest.Initializer.class)
public class WbListManagerApplicationTest extends KafkaAbstractTest {

    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";

    @LocalServerPort
    int serverPort;

    private static String SERVICE_URL = "http://localhost:%s/v1/wb_list";

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private RiakClient client;

    @Value("${kafka.wblist.topic.command}")
    public String topic;
    
    @Value("${riak.bucket}")
    private String BUCKET_NAME;

    @Value("${kafka.wblist.topic.event.sink}")
    public String topicEventSink;

    @ClassRule
    public static GenericContainer riak = new GenericContainer("basho/riak-kv")
            .withExposedPorts(8098, 8087)
            .withPrivilegedMode(true)
            .waitingFor(new HttpWaitStrategy()
                    .forStatusCode(200)
                    .forPath("/ping"))
            .withStartupTimeout(Duration.ofMinutes(5));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("riak.port=" + riak.getMappedPort(8087))
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    public void riakTest() throws ExecutionException, InterruptedException {
        Row row = new Row();
        row.setKey(KEY);
        row.setValue(VALUE);
        listRepository.create(row);

        Namespace ns = new Namespace(BUCKET_NAME);
        Location location = new Location(ns, KEY);
        FetchValue fv = new FetchValue.Builder(location).build();
        FetchValue.Response response = client.execute(fv);
        RiakObject obj = response.getValue(RiakObject.class);

        String result = obj.getValue().toString();
        Assert.assertEquals(VALUE, result);

        Optional<Row> resultGet = listRepository.get(KEY);
        Assert.assertFalse(resultGet.isEmpty());
        Assert.assertEquals(VALUE, resultGet.get().getValue());

        listRepository.remove(row);
        response = client.execute(fv);
        obj = response.getValue(RiakObject.class);
        Assert.assertNull(obj);

    }

    @Test
    public void kafkaRowTest() throws Exception {
        THClientBuilder clientBuilder = new THClientBuilder()
                .withAddress(new URI(String.format(SERVICE_URL, serverPort)))
                .withNetworkTimeout(300000);
        WbListServiceSrv.Iface iface = clientBuilder.build(WbListServiceSrv.Iface.class);

        Producer<String, ChangeCommand> producer = createProducer();
        ChangeCommand changeCommand = createCommand();
        ProducerRecord<String, ChangeCommand> producerRecord = new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producer.send(producerRecord).get();
        producer.close();
        Thread.sleep(1000L);

        boolean exist = iface.isExist(changeCommand.getRow());
        Assert.assertTrue(exist);

        producer = createProducer();
        changeCommand.setCommand(Command.DELETE);
        producerRecord = new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producer.send(producerRecord).get();
        producer.close();
        Thread.sleep(1000L);

        exist = iface.isExist(changeCommand.getRow());
        Assert.assertFalse(exist);

        Consumer<String, Event> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList(topicEventSink));

        List<Event> eventList = new ArrayList<>();
        ConsumerRecords<String, Event> consumerRecords =
                consumer.poll(Duration.ofSeconds(1));
        consumerRecords.forEach(record -> {
            log.info("poll message: {}", record.value());
            eventList.add(record.value());});
        consumer.close();

        Assert.assertEquals(2, eventList.size());
    }

    @NotNull
    private ChangeCommandWrapper createCommand() {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setShopId(SHOP_ID);
        row.setPartyId(PARTY_ID);
        row.setListName(LIST_NAME);
        row.setListType(ListType.black);
        row.setValue(VALUE);
        changeCommand.setRow(row);
        return changeCommand;
    }

    public static Consumer<String, Event> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "CLIENT");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return new KafkaConsumer<>(props);
    }

}
