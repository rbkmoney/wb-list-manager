package com.rbkmoney.wb.list.manager;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.damsel.wb_list.Command;
import com.rbkmoney.testcontainers.annotations.KafkaSpringBootTest;
import com.rbkmoney.testcontainers.annotations.kafka.KafkaTestcontainer;
import com.rbkmoney.testcontainers.annotations.kafka.config.KafkaProducer;
import com.rbkmoney.wb.list.manager.config.MockedStartupInitializers;
import com.rbkmoney.wb.list.manager.exception.RiakExecutionException;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@KafkaSpringBootTest
@TestPropertySource(properties = {"retry.timeout=100"})
@KafkaTestcontainer(topicsKeys = {"kafka.wblist.topic.command", "kafka.wblist.topic.event.sink"})
@Import(MockedStartupInitializers.class)
public class WbListSafetyApplicationTest {

    @Value("${kafka.wblist.topic.command}")
    public String topic;

    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;

    @MockBean
    private ListRepository listRepository;

    @Test
    void kafkaRowTestException() throws Exception {
        doThrow(new RiakExecutionException(),
                new RiakExecutionException())
                .doNothing()
                .when(listRepository).create(any());
        ChangeCommand changeCommand = TestObjectFactory.testCommand();
        changeCommand.setCommand(Command.CREATE);

        testThriftKafkaProducer.send(topic, changeCommand);

        verify(listRepository, timeout(2000L).times(3)).create(any());
    }

}
