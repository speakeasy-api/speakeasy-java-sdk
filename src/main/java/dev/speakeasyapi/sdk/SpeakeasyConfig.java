package dev.speakeasyapi.sdk;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class SpeakeasyConfig {

    private String serverUrl = "grpc.prod.speakeasyapi.dev:443";
    private boolean secureGrpc = true;
    private boolean ingestEnabled = true;

    protected SpeakeasyConfig() {
        String serverURL = System.getenv("SPEAKEASY_SERVER_URL");
        if (serverURL != null) {
            this.serverUrl = serverURL;
        }

        if ("false".equals(System.getenv("SPEAKEASY_SERVER_SECURE"))) {
            this.secureGrpc = false;
        }

        if ("true".equals(System.getenv("SPEAKEASY_TEST_MODE"))) {
            this.ingestEnabled = false;
        }
    }

    public void validate() throws IllegalArgumentException {
        if (StringUtils.isEmpty(getApiKey())) {
            throw new IllegalArgumentException("Speakeasy API key is required.");
        }

        int maxIDSize = 128;
        String validCharsRegexStr = "[^a-zA-Z0-9.\\-_~]";

        if (StringUtils.isEmpty(getApiID())) {
            throw new IllegalArgumentException("ApiID is required.");
        }

        if (getApiID().length() > maxIDSize) {
            throw new IllegalArgumentException("ApiID must be less than " + maxIDSize + " characters.");
        }

        Pattern pattern = Pattern.compile(validCharsRegexStr);
        Matcher matcher = pattern.matcher(getApiID());
        if (matcher.find()) {
            throw new IllegalArgumentException("ApiID contains invalid characters " + validCharsRegexStr);
        }

        if (StringUtils.isEmpty(getVersionID())) {
            throw new IllegalArgumentException("VersionID is required.");
        }

        if (getVersionID().length() > maxIDSize) {
            throw new IllegalArgumentException("VersionID must be less than " + maxIDSize + " characters.");
        }

        pattern = Pattern.compile(validCharsRegexStr);
        matcher = pattern.matcher(getVersionID());
        if (matcher.find()) {
            throw new IllegalArgumentException("VersionID contains invalid characters " + validCharsRegexStr);
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public boolean isSecureGrpc() {
        return secureGrpc;
    }

    public boolean isIngestEnabled() {
        return ingestEnabled;
    }

    abstract public String getApiKey();

    abstract public void setApiKey(String apiKey);

    abstract public String getApiID();

    abstract public void setApiID(String apiID);

    abstract public String getVersionID();

    abstract public void setVersionID(String versionID);
}
