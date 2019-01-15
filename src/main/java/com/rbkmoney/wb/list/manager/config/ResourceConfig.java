package com.rbkmoney.wb.list.manager.config;

import com.rbkmoney.damsel.wb_list.WbListServiceSrv;
import com.rbkmoney.wb.list.manager.handler.WbListServiceHandler;
import com.rbkmoney.wb.list.manager.repository.ListRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceConfig {

    @Bean
    public WbListServiceSrv.Iface fraudInspectorHandler(ListRepository listRepository) {
        return new WbListServiceHandler(listRepository);
    }

}
