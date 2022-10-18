package dev.speakeasyapi.springboot;

import java.io.ByteArrayOutputStream;
import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.smartbear.har.model.HarCreator;

import dev.speakeasyapi.sdk.SpeakeasyHarBuilder;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;

public class SpeakeasyRequestResponseHandler implements Runnable {
    private final String sdkName = "speakeasy-java-sdk";
    private final String speakeasyVersion = "1.2.3";
    private final ISpeakeasyClient speakeasyClient;
    private final Logger logger;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final RequestResponseCaptureWatcher watcher;
    private final Instant startTime;
    private final Instant endTime;
    private final String pathHint;
    private final String customerID;

    public SpeakeasyRequestResponseHandler(ISpeakeasyClient speakeasyClient, Logger logger,
            HttpServletRequest request,
            HttpServletResponse response,
            RequestResponseCaptureWatcher watcher,
            Instant startTime, Instant endTime, String pathHint, String customerID) {
        this.speakeasyClient = speakeasyClient;
        this.logger = logger;
        this.request = request;
        this.response = response;
        this.watcher = watcher;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pathHint = pathHint;
        this.customerID = customerID;
    }

    @Override
    public void run() {
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(this.request.getRequestURI())
                .build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            new SpeakeasyHarBuilder()
                    .withStartTime(this.startTime)
                    .withEndTime(this.endTime)
                    .withComment(String.format("request capture for %s", this.request.getRequestURI()))
                    .withCreator(new HarCreator(this.sdkName, "", this.speakeasyVersion))
                    .withHostName(uriComponents.getHost())
                    .withOutputStream(outputStream)
                    .withPort(uriComponents.getPort())
                    .withRequest(this.request, this.watcher.getRequestCache(), this.watcher.getRequestIsValid(),
                            this.logger)
                    .withResponse(this.response, this.watcher.getResponseCache(), this.watcher.getResponseIsValid(),
                            this.request.getProtocol(), this.logger)
                    .build();
        } catch (Exception e) {
            logger.debug("speakeasy-sdk: Failed to build Har file", e);
            return;
        }

        String harString = outputStream.toString();
        try {
            speakeasyClient.ingestGrpc(harString, pathHint, customerID);
        } catch (Exception e) {
            logger.debug("speakeasy-sdk: Failed to ingest request:", e);
        }
    }
}
