package dev.speakeasyapi.springboot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.speakeasyapi.sdk.SpeakeasyConfig;
import org.apache.commons.lang3.StringUtils;

public class SpeakeasySpringBootConfig extends SpeakeasyConfig {
    private String apiKey;
    private String apiID;
    private String versionID;

    public SpeakeasySpringBootConfig() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiID() {
        return apiID;
    }

    public void setApiID(String apiID) {
        this.apiID = apiID;
    }

    public String getVersionID() {
        return versionID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }
}
