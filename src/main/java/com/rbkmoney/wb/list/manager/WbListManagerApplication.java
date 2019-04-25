package com.rbkmoney.wb.list.manager;

import com.basho.riak.client.api.RiakClient;
import com.rbkmoney.wb.list.manager.listener.StartupListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PreDestroy;

@ServletComponentScan
@SpringBootApplication
public class WbListManagerApplication extends SpringApplication {

    @Autowired
    private StartupListener startupListener;
    @Autowired
    private RiakClient client;

    public static void main(String[] args) {
        SpringApplication.run(WbListManagerApplication.class);
    }

    @PreDestroy
    public void preDestroy() {
        startupListener.stop();
        client.shutdown();
    }
}
