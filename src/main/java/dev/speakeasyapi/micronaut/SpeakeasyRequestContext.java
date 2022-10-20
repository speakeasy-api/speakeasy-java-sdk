package dev.speakeasyapi.micronaut;

import java.time.Instant;

import dev.speakeasyapi.sdk.SpeakeasyConfig;
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;
import dev.speakeasyapi.sdk.client.SpeakeasyClient;

public class SpeakeasyRequestContext {
    private final SpeakeasyConfig cfg;
    private final SpeakeasyClient client;
    private final SpeakeasyMiddlewareController controller;
    private final Instant startTime;

    public SpeakeasyRequestContext(SpeakeasyConfig cfg) {
        this.cfg = cfg;
        this.client = new SpeakeasyClient(cfg);
        this.controller = new SpeakeasyMiddlewareController(this.client);
        this.startTime = Instant.now();
    }

    public SpeakeasyConfig getConfig() {
        return cfg;
    }

    public SpeakeasyClient getClient() {
        return client;
    }

    public SpeakeasyMiddlewareController getController() {
        return controller;
    }

    public Instant getStartTime() {
        return startTime;
    }
}
