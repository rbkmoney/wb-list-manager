package com.rbkmoney.wb.list.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class WbListManagerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(WbListManagerApplication.class);
    }

}
