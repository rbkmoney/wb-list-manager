package com.rbkmoney.wb.list.manager.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;

public class AwaitilityExtension implements BeforeAllCallback {

    public static final long TIMEOUT = 10_000L;
    public static final long POLL_INTERVAL = 500L;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Awaitility.setDefaultPollInterval(TIMEOUT, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultTimeout(POLL_INTERVAL, TimeUnit.MILLISECONDS);
    }

}
