package com.rbkmoney.wb.list.manager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class MockedStartupInitializers {

    @MockBean
    private RiakInitializer riakInitializer;

}
