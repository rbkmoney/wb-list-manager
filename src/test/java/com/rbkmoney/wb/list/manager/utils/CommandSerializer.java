package com.rbkmoney.wb.list.manager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class CommandSerializer implements Serializer<ChangeCommandWrapper> {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, ChangeCommandWrapper data) {
        byte[] retVal = null;
        try {
            SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
                    .serializeAllExcept("fieldMetaData");
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("myFilter", theFilter);
            retVal = om.writer(filters).writeValueAsString(data).getBytes();
        } catch (Exception e) {
            log.error("Error when serialize ThriftSerializer data: {} ", data, e);
        }
        return retVal;
    }

    @Override
    public void close() {

    }

}
