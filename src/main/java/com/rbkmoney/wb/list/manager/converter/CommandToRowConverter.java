package com.rbkmoney.wb.list.manager.converter;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.utils.KeyGenerator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class CommandToRowConverter implements Converter<ChangeCommand, Row> {

    @Override
    public Row convert(ChangeCommand command) {
        Row row = new Row();
        String bucket = KeyGenerator.generateBucket(command.getPartyId(), command.getShopId());
        row.setBucketName(bucket);
        String key = KeyGenerator.generateKey(command.getListName(), command.getValue());
        row.setKey(key);
        row.setValue(command.getValue());
        return row;
    }
}
