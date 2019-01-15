package com.rbkmoney.wb.list.manager.serializer;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.ChangeCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

@Slf4j
public class CommandDeserializer implements Deserializer<ChangeCommand> {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ChangeCommand deserialize(String topic, byte[] data) {
        ChangeCommand command = null;
        try {
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            command = om.readValue(data, ChangeCommand.class);
        } catch (Exception e) {
            log.error("Error when deserialize command data: {} ", data, e);
        }
        return command;
    }

    @Override
    public void close() {

    }
}