package com.rbkmoney.wb.list.manager.serializer;


import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.serializer.ThriftSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Slf4j
public class CommandSerde implements Serde<ChangeCommand> {


    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<ChangeCommand> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<ChangeCommand> deserializer() {
        return new CommandDeserializer();
    }
}
