package com.rbkmoney.wb.list.manager.listener;

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

    @KafkaListener(topics = "${kafka.wblist.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(Row row) {
        log.info("TemplateListener ruleTemplate: {}", row);
        listRepository.create(row);
    }
}
