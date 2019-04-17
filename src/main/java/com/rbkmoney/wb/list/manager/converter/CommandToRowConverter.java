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
        com.rbkmoney.damsel.wb_list.Row commandRow = command.getRow();
        String key = KeyGenerator.generateKey(commandRow.getPartyId(), commandRow.getShopId(), commandRow.getListName(), commandRow.getValue());
        row.setKey(key);
        row.setValue(commandRow.getValue());
        return row;
    }
}
