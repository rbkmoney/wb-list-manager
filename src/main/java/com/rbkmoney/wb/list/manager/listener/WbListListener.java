package com.rbkmoney.wb.list.manager.listener;

import com.rbkmoney.damsel.wb_list.ChangeCommand;
import com.rbkmoney.wb.list.manager.converter.CommandToRowConverter;
import com.rbkmoney.wb.list.manager.model.Row;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WbListListener {

    private final ListRepository listRepository;
    private final CommandToRowConverter commandToRowConverter;

    @KafkaListener(topics = "${kafka.wblist.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ChangeCommand command) {
        log.info("TemplateListener ruleTemplate: {}", command);
        try {
            Row row = commandToRowConverter.convert(command);
            switch (command.command) {
                case CREATE:
                    listRepository.create(row);
                    break;
                case DELETE:
                    listRepository.remove(row);
                    break;
                default:
                    log.warn("WbListListener command for list not found! command: {}", command);
            }
        } catch (Exception e) {
            log.error("Error WbListListener listen command: {} e:", command, e);
        }
    }
}
