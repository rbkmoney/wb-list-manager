package com.rbkmoney.wb.list.manager;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.damsel.wb_list.Command;
import com.rbkmoney.damsel.wb_list.Event;
import com.rbkmoney.damsel.wb_list.ListType;
import com.rbkmoney.wb.list.manager.exception.RiakExecutionException;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import com.rbkmoney.wb.list.manager.serializer.EventDeserializer;
import com.rbkmoney.wb.list.manager.utils.ChangeCommandWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

//@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {"retry.timeout=100"})
@ContextConfiguration(classes = WbListManagerApplication.class, initializers = WbListSafetyApplicationTest.Initializer.class)
public class WbListSafetyApplicationTest extends KafkaAbstractTest {

    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";

    @MockBean
    private ListRepository listRepository;

    @Value("${kafka.wblist.topic.command}")
    public String topic;
    
    @Value("${riak.bucket}")
    private String BUCKET_NAME;

    @Value("${kafka.wblist.topic.event.sink}")
    public String topicEventSink;

    @Test
    public void kafkaRowTestException() throws Exception {

        doThrow(new RiakExecutionException(),
                new RiakExecutionException(),
                new RiakExecutionException()).when(listRepository).create(any());

        Producer<String, ChangeCommand> producerNew = createProducer();
        ChangeCommand changeCommand = createCommand();
        changeCommand.setCommand(Command.CREATE);
        ProducerRecord<String, ChangeCommand> producerRecordCommand = new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producerNew.send(producerRecordCommand).get();
        producerNew.close();
        Thread.sleep(2000L);

        Mockito.verify(listRepository, times(3)).create(any());
    }

    @NotNull
    private ChangeCommandWrapper createCommand() {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        com.rbkmoney.damsel.wb_list.Row row = createRow();
        changeCommand.setRow(row);
        return changeCommand;
    }

    @NotNull
    private com.rbkmoney.damsel.wb_list.Row createRow() {
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setShopId(SHOP_ID);
        row.setPartyId(PARTY_ID);
        row.setListName(LIST_NAME);
        row.setListType(ListType.black);
        row.setValue(VALUE);
        return row;
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
