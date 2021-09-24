package com.rbkmoney.wb.list.manager;

import com.rbkmoney.damsel.wb_list.*;
import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import com.rbkmoney.wb.list.manager.exception.RiakExecutionException;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import com.rbkmoney.wb.list.manager.utils.ChangeCommandWrapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {"retry.timeout=100"})
public class WbListSafetyApplicationTest extends KafkaAbstractTest {

    private static final String VALUE = "value";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";

    @Value("${kafka.wblist.topic.command}")
    public String topic;

    @Value("${kafka.wblist.topic.event.sink}")
    public String topicEventSink;

    @MockBean
    private ListRepository listRepository;

    public static <T> Producer<String, T> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "CLIENT");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ThriftSerializer.class);
        return new KafkaProducer<>(props);
    }

    @Test
    void kafkaRowTestException() throws Exception {
        doThrow(new RiakExecutionException(),
                new RiakExecutionException())
                .doNothing()
                .when(listRepository).create(any());

        Producer<String, ChangeCommand> producerNew = createProducer();
        ChangeCommand changeCommand = createCommand();
        changeCommand.setCommand(Command.CREATE);
        ProducerRecord<String, ChangeCommand> producerRecordCommand =
                new ProducerRecord<>(topic, changeCommand.getRow().getValue(), changeCommand);
        producerNew.send(producerRecordCommand).get();
        producerNew.close();

        Thread.sleep(2000L);

        Mockito.verify(listRepository, times(3)).create(any());
    }

    private ChangeCommandWrapper createCommand() {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        com.rbkmoney.damsel.wb_list.Row row = createRow();
        changeCommand.setRow(row);
        return changeCommand;
    }

    private com.rbkmoney.damsel.wb_list.Row createRow() {
        com.rbkmoney.damsel.wb_list.Row row = new com.rbkmoney.damsel.wb_list.Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID))
        );
        row.setListName(LIST_NAME);
        row.setListType(ListType.black);
        row.setValue(VALUE);
        return row;
    }

}
