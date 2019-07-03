package com.rbkmoney.wb.list.manager.serializer;


import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandDeserializer extends AbstractThriftDeserializer<ChangeCommand> {

    @Override
    public ChangeCommand deserialize(String topic, byte[] data) {
        return super.deserialize(data, new ChangeCommand());
    }
}