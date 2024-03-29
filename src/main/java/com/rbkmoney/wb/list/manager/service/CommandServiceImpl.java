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
        log.info("CommandService apply command: {}", command);
        Row row = commandToRowConverter.convert(command);
        log.info("CommandService apply row: {}", row);
        Event event = applyCommandAndGetEvent(command, row);
        event.setRow(command.getRow());
        return event;
    }

    private Event applyCommandAndGetEvent(ChangeCommand command, Row row) {
        return switch (command.getCommand()) {
            case CREATE -> {
                listRepository.create(row);
                yield new Event().setEventType(EventType.CREATED);
            }
            case DELETE -> {
                listRepository.remove(row);
                yield new Event().setEventType(EventType.DELETED);
            }
            default -> {
                log.warn("CommandService command for list not found! command: {}", command);
                throw new RuntimeException("WbListStreamFactory command for list not found!");
            }
        };
    }

}
