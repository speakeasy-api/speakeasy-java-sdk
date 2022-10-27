package dev.speakeasyapi.micronaut;

import dev.speakeasyapi.sdk.SpeakeasyConfig;
import io.micronaut.context.annotation.Value;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class SpeakeasyMicronautConfig extends SpeakeasyConfig {
    private String serverUrl = "grpc.prod.speakeasyapi.dev:443";
    private boolean secureGrpc = true;
    private boolean ingestEnabled = true;

//    @Value("${speakeasyapi.apikey}")
    private String apiKey;
//    @Value("${speakeasyapi.apiid}")
    private String apiID;
//    @Value("${speakeasyapi.versionID}")
    private String versionID;

    public SpeakeasyMicronautConfig() {
        readProperties();
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

    private void readProperties() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream("application.yml");
        if (inputStream == null) {
            throw new IllegalArgumentException("The application.yml file was not found in the resources directory.");
        }
        Map<String, Object> properties = yaml.load(inputStream);
        Map<String,String> speakeasyProperties = (Map<String, String>) properties.get("speakeasyApi");
        if (speakeasyProperties == null) {
            throw new IllegalArgumentException("The speakeasyApi property was not found in the application.yml.");
        }
        apiKey = speakeasyProperties.get("apiKey");
        apiID = speakeasyProperties.get("apiID");
        versionID = speakeasyProperties.get("versionID");
    }
}
