package com.rbkmoney.wb.list.manager.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.damsel.wb_list.CountInfo;
import com.rbkmoney.wb.list.manager.model.CountInfoModel;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CommandToRowConverter implements Converter<ChangeCommand, Row> {

    private final ObjectMapper objectMapper;

    @Override
    public Row convert(ChangeCommand command) {
        Row row = new Row();
        com.rbkmoney.damsel.wb_list.Row commandRow = command.getRow();
        String key = KeyGenerator.generateKey(commandRow);
        row.setKey(key);
        row.setValue(initValue(commandRow));
        return row;
    }

    private String initValue(com.rbkmoney.damsel.wb_list.Row commandRow) {
        if (commandRow.isSetRowInfo() && commandRow.getRowInfo().isSetCountInfo()) {
            try {
                CountInfo countInfo = commandRow.getRowInfo().getCountInfo();
                CountInfoModel countInfoModel = new CountInfoModel(countInfo.getCount(),
                        countInfo.getTimeToLive(),
                        countInfo.getStartCountTime());
                return objectMapper.writeValueAsString(countInfoModel);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return commandRow.getValue();
    }
}
