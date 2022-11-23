package dev.speakeasyapi.springboot;

import java.io.ByteArrayOutputStream;
import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import dev.speakeasyapi.sdk.SpeakeasyHarBuilder;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;
import dev.speakeasyapi.sdk.masking.Masking;

public class SpeakeasyRequestResponseHandler implements Runnable {
    private final ISpeakeasyClient speakeasyClient;
    private final Logger logger;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final RequestResponseCaptureWatcher watcher;
    private final Instant startTime;
    private final Instant endTime;
    private final String pathHint;
    private final String customerID;
    private final Masking masking;

    public SpeakeasyRequestResponseHandler(ISpeakeasyClient speakeasyClient, Logger logger,
            HttpServletRequest request,
            HttpServletResponse response,
            RequestResponseCaptureWatcher watcher,
            Masking masking,
            Instant startTime, Instant endTime, String pathHint, String customerID) {
        this.speakeasyClient = speakeasyClient;
        this.logger = logger;
        this.request = request;
        this.response = response;
        this.watcher = watcher;
        this.masking = masking;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pathHint = pathHint;
        this.customerID = customerID;
    }

    @Override
    public void run() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            new SpeakeasyHarBuilder(this.logger)
                    .withStartTime(this.startTime)
                    .withEndTime(this.endTime)
                    .withOutputStream(outputStream)
                    .withMasking(masking)
                    .withRequest(new SpeakeasyServletRequest(this.request, this.watcher))
                    .withResponse(new SpeakeasyServletResponse(this.response, this.watcher), this.request.getProtocol())
                    .build();
        } catch (Exception e) {
            logger.debug("speakeasy-sdk: Failed to build Har file", e);
            return;
        }

        String harString = outputStream.toString();
        try {
            speakeasyClient.ingestGrpc(harString, pathHint, customerID, masking);
        } catch (Exception e) {
            logger.debug("speakeasy-sdk: Failed to ingest request:", e);
        }
    }
}
