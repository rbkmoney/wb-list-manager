package com.rbkmoney.wb.list.manager;

import com.rbkmoney.damsel.wb_list.*;
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
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class WbListManagerApplicationTest extends KafkaAbstractTest {

    public static final String IDENTITY_ID = "identityId";
    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";
    private static String SERVICE_URL = "http://localhost:%s/wb_list/v1";

    @Value("${kafka.wblist.topic.command}")
    public String topic;

    @Value("${kafka.wblist.topic.event.sink}")
    public String topicEventSink;

    @LocalServerPort
    int serverPort;

    @Value("${riak.bucket}")
    private String bucketName;

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

    @Test
    void kafkaRowTest() throws Exception {
        THClientBuilder clientBuilder = new THClientBuilder()
                .withAddress(new URI(String.format(SERVICE_URL, serverPort)))
                .withNetworkTimeout(300000);
        WbListServiceSrv.Iface iface = clientBuilder.build(WbListServiceSrv.Iface.class);

        ChangeCommand changeCommand = produceCreateRow(createRow());

        boolean exist = iface.isExist(changeCommand.getRow());
        assertTrue(exist);

        produceDeleteRow(changeCommand);

        exist = iface.isExist(changeCommand.getRow());
        assertFalse(exist);

        Consumer<String, Event> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList(topicEventSink));

        List<Event> eventList = new ArrayList<>();
        ConsumerRecords<String, Event> consumerRecords =
                consumer.poll(Duration.ofSeconds(1));
        consumerRecords.forEach(record -> {
            log.info("poll message: {}", record.value());
            eventList.add(record.value());
        });
        consumer.close();

        assertEquals(2, eventList.size());

        Producer<String, ChangeCommand> producer = createProducer();
        Row row = createRowOld();
        changeCommand = createCommand(row);
        row.setShopId(null);

        ProducerRecord<String, ChangeCommand> producerRecord =
                new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producer.send(producerRecord).get();
        producer.close();
        Thread.sleep(1000L);

        exist = iface.isExist(row);
        assertTrue(exist);

        row.setShopId(SHOP_ID);

        exist = iface.isExist(row);
        assertTrue(exist);

        Result info = iface.getRowInfo(row);
        assertFalse(info.isSetRowInfo());

        row.setListType(ListType.grey);

        //check without partyId and shop id
        createRow(Instant.now().toString(), null, null);
        RowInfo rowInfo = iface.getRowInfo(row).getRowInfo();
        assertEquals(5, rowInfo.getCountInfo().getCount());

        //check without partyId
        createRow(Instant.now().toString(), null, SHOP_ID);
        rowInfo = iface.getRowInfo(row).getRowInfo();
        assertEquals(5, rowInfo.getCountInfo().getCount());

        //check full key field
        createRow(Instant.now().toString(), PARTY_ID, SHOP_ID);
        rowInfo = iface.getRowInfo(row).getRowInfo();
        assertEquals(5, rowInfo.getCountInfo().getCount());

        rowInfo = checkCreateWithCountInfo(iface, Instant.now().toString(), PARTY_ID, SHOP_ID);

        assertFalse(rowInfo.getCountInfo().getStartCountTime().isEmpty());

        Row rowP2p = createListRow();
        rowP2p.setId(IdInfo.p2p_id(new P2pId().setIdentityId(IDENTITY_ID)));

        changeCommand = produceCreateRow(rowP2p);

        exist = iface.isExist(changeCommand.getRow());
        assertTrue(exist);

        produceDeleteRow(changeCommand);

        exist = iface.isExist(changeCommand.getRow());
        assertFalse(exist);
    }

    private void produceDeleteRow(ChangeCommand changeCommand)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        Producer<String, ChangeCommand> producer = createProducer();
        changeCommand.setCommand(Command.DELETE);
        ProducerRecord<String, ChangeCommand> producerRecord =
                new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producer.send(producerRecord).get();
        producer.close();
        Thread.sleep(1000L);
    }

    private ChangeCommand produceCreateRow(com.rbkmoney.damsel.wb_list.Row row)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        Producer<String, ChangeCommand> producer = createProducer();
        ChangeCommand changeCommand = createCommand(row);
        ProducerRecord<String, ChangeCommand> producerRecord =
                new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producer.send(producerRecord).get();
        producer.close();
        Thread.sleep(1000L);
        return changeCommand;
    }

    private RowInfo checkCreateWithCountInfo(WbListServiceSrv.Iface iface, String startTimeCount, String partyId,
                                             String shopId)
            throws InterruptedException, java.util.concurrent.ExecutionException, TException {
        Row rowWithCountInfo = createRow(startTimeCount, partyId, shopId);
        return iface.getRowInfo(rowWithCountInfo).getRowInfo();
    }

    private Row createRow(String startTimeCount, String partyId, String shopId)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        Producer<String, ChangeCommand> producer;
        ChangeCommand changeCommand;
        ProducerRecord<String, ChangeCommand> producerRecord;
        producer = createProducer();
        Row rowWithCountInfo = createRowWithCountInfo(startTimeCount, partyId, shopId);
        changeCommand = createCommand(rowWithCountInfo);
        producerRecord = new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producer.send(producerRecord).get();
        producer.close();
        Thread.sleep(1000L);

        return rowWithCountInfo;
    }

    private com.rbkmoney.damsel.wb_list.Row createRow() {
        Row row = createListRow();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID)
        ));
        return row;
    }

    private ChangeCommandWrapper createCommand(com.rbkmoney.damsel.wb_list.Row row) {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        changeCommand.setRow(row);
        return changeCommand;
    }

    private com.rbkmoney.damsel.wb_list.Row createRowOld() {
        Row row = createListRow()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID);
        return row;
    }

    private Row createListRow() {
        Row row = new Row();
        row.setListName(LIST_NAME);
        row.setListType(ListType.black);
        row.setValue(VALUE);
        return row;
    }

    private com.rbkmoney.damsel.wb_list.Row createRowWithCountInfo(String startTimeCount, String partyId,
                                                                   String shopId) {
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID)
        ));
        row.setListName(LIST_NAME);
        row.setListType(ListType.grey);
        row.setValue(VALUE);
        row.setRowInfo(RowInfo.count_info(
                new CountInfo()
                        .setCount(5L)
                        .setStartCountTime(startTimeCount)
                        .setTimeToLive(Instant.now().plusSeconds(6000L).toString()))
        );
        return row;
    }

}
