package dev.speakeasyapi.sdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class SpeakeasyConfig {
    private String serverUrl = "grpc.prod.speakeasyapi.dev:443";
    private boolean secureGrpc = true;
    private boolean ingestEnabled = true;

    private String apiKey;
    private String apiID;
    private String versionID;

    public SpeakeasyConfig() {
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
        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException("Speakeasy API key is required.");
        }

        int maxIDSize = 128;
        String validCharsRegexStr = "[^a-zA-Z0-9.\\-_~]";

        if (StringUtils.isEmpty(apiID)) {
            throw new IllegalArgumentException("ApiID is required.");
        }

        if (apiID.length() > maxIDSize) {
            throw new IllegalArgumentException("ApiID must be less than " + maxIDSize + " characters.");
        }

        Pattern pattern = Pattern.compile(validCharsRegexStr);
        Matcher matcher = pattern.matcher(apiID);
        if (matcher.find()) {
            throw new IllegalArgumentException("ApiID contains invalid characters " + validCharsRegexStr);
        }

        if (StringUtils.isEmpty(versionID)) {
            throw new IllegalArgumentException("VersionID is required.");
        }

        if (versionID.length() > maxIDSize) {
            throw new IllegalArgumentException("VersionID must be less than " + maxIDSize + " characters.");
        }

        pattern = Pattern.compile(validCharsRegexStr);
        matcher = pattern.matcher(versionID);
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
