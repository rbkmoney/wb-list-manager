package com.rbkmoney.wb.list.manager.service;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.damsel.wb_list.Event;
import com.rbkmoney.damsel.wb_list.EventType;
import com.rbkmoney.wb.list.manager.converter.CommandToRowConverter;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {

    private final CommandToRowConverter commandToRowConverter;
    private final ListRepository listRepository;

    @Override
    public Event apply(ChangeCommand command) {
        log.info("WbListStreamFactory apply command: {}", command);
        Event event = new Event();
        Row row = commandToRowConverter.convert(command);
        log.info("WbListStreamFactory apply row: {}", row);
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
