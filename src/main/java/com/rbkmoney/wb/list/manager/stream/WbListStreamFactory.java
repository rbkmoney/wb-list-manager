package com.rbkmoney.wb.list.manager.stream;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.damsel.wb_list.Event;
import com.rbkmoney.damsel.wb_list.EventType;
import com.rbkmoney.wb.list.manager.converter.CommandToRowConverter;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import com.rbkmoney.wb.list.manager.serializer.CommandSerde;
import com.rbkmoney.wb.list.manager.serializer.EventSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class WbListStreamFactory {

    @Value("${kafka.wblist.topic.command}")
    private String readTopic;
    @Value("${kafka.wblist.topic.event.sink}")
    private String resultTopic;

    private final CommandSerde commandSerde = new CommandSerde();
    private final EventSerde eventSerde = new EventSerde();
    private final CommandToRowConverter commandToRowConverter;
    private final ListRepository listRepository;

    public KafkaStreams create(final Properties streamsConfiguration) {
        try {
            StreamsBuilder builder = new StreamsBuilder();
            builder.stream(readTopic, Consumed.with(Serdes.String(), commandSerde))
                    .filter((s, changeCommand) -> changeCommand != null && changeCommand.getCommand() != null)
                    .peek((s, changeCommand) -> log.debug("Command stream check command: {}", changeCommand))
                    .mapValues(this::apply)
                    .to(resultTopic, Produced.with(Serdes.String(), eventSerde));
            return new KafkaStreams(builder.build(), streamsConfiguration);
        } catch (Exception e) {
            log.error("WbListStreamFactory error when create stream e: ", e);
            throw new RuntimeException(e);
        }
    }

    private Event apply(ChangeCommand command) {
        Event event = new Event();
        Row row = commandToRowConverter.convert(command);
        switch (command.command) {
            case CREATE:
                listRepository.create(row);
                event.setEventType(EventType.CREATED);
                break;
            case DELETE:
                listRepository.remove(row);
                event.setEventType(EventType.DELETED);
                break;
            default:
                log.warn("WbListStreamFactory command for list not found! command: {}", command);
                throw new RuntimeException("WbListStreamFactory command for list not found!");
        }
        event.setRow(command.getRow());
        return event;
    }
}
