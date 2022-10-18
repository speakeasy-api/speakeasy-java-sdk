package dev.speakeasyapi.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConfigurationProperties(prefix = "speakeasy-api")
@ConfigurationPropertiesScan
@Import(SpeakeasyFilter.class)
public class EnableSpeakeasy implements WebMvcConfigurer {
    private String apiKey;
    private String apiID;
    private String versionID;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiID(String apiID) {
        this.apiID = apiID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpeakeasyInterceptor(apiKey, apiID, versionID));
    }
}
