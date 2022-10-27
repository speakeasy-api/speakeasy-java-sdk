package dev.speakeasyapi.micronaut.implementation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.speakeasyapi.micronaut.SpeakeasyMicronautConfig;
import dev.speakeasyapi.sdk.SpeakeasyConfig;

public final class SpeakeasySingleton {
    private static SpeakeasySingleton INSTANCE;

    private final SpeakeasyConfig cfg = new SpeakeasyMicronautConfig();
    private final Map<String, SpeakeasyRequestContext> requests = new ConcurrentHashMap<>();

    private SpeakeasySingleton() {
    }

    synchronized public static SpeakeasySingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpeakeasySingleton();
        }
        return INSTANCE;
    }

    public void configure(String apiKey, String apiID, String versionID) {
        this.cfg.setApiKey(apiKey);
        this.cfg.setApiID(apiID);
        this.cfg.setVersionID(versionID);
    }

    public SpeakeasyConfig getConfig() {
        return cfg;
    }

    public void registerRequest(final String requestId) {
        requests.put(requestId, new SpeakeasyRequestContext(cfg));
    }

    public void removeRequest(final String requestId) {
        requests.remove(requestId);
    }

    public SpeakeasyRequestContext getRequestContext(final String requestId) {
        return requests.get(requestId);
    }
}
