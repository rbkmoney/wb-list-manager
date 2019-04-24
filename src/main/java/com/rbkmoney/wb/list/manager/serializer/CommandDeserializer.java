package com.rbkmoney.wb.list.manager.serializer;


import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.deserializer.AbstractDeserializerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandDeserializer extends AbstractDeserializerAdapter<ChangeCommand> {

    @Override
    public ChangeCommand deserialize(String topic, byte[] data) {
        return super.deserialize(data, new ChangeCommand());
    }
}