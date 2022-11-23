package dev.speakeasyapi.micronaut.implementation;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.MoreExecutors;

import dev.speakeasyapi.sdk.SpeakeasyConfig;
import dev.speakeasyapi.sdk.SpeakeasyHarBuilder;
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;

public class SpeakeasyCapture implements Runnable {
    private Executor pool;
    private SpeakeasyNettyRequest request;
    private SpeakeasyNettyResponse response;

    private Logger logger = LoggerFactory.getLogger(SpeakeasyCapture.class);

    public SpeakeasyCapture() {
        SpeakeasyConfig cfg = SpeakeasySingleton.getInstance().getConfig();

        this.pool = Executors.newCachedThreadPool();

        if (!cfg.isIngestEnabled()) {
            this.pool = MoreExecutors.directExecutor();
        }
    }

    public void capture(final SpeakeasyNettyRequest request, final SpeakeasyNettyResponse response) {
        this.request = request;
        this.response = response;

        pool.execute(this);
    }

    public void run() {
        Instant endTime = Instant.now();

        SpeakeasyRequestContext context = SpeakeasySingleton.getInstance()
                .getRequestContext(request.getRequestId());

        if (context != null) {
            SpeakeasyMiddlewareController controller = context.getController();
            if (!controller.isEnabled()) {
                return;
            }

            request.removeRequestId();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                new SpeakeasyHarBuilder(this.logger)
                        .withStartTime(context.getStartTime())
                        .withEndTime(endTime)
                        .withOutputStream(outputStream)
                        .withMasking(controller.getMasking())
                        .withRequest(this.request)
                        .withResponse(this.response, this.request.getProtocol())
                        .build();
            } catch (Exception e) {
                logger.debug("speakeasy-sdk: Failed to build Har file", e);
                return;
            }

            String harString = outputStream.toString();
            try {
                context.getClient().ingestGrpc(harString, controller.getPathHint(), controller.getCustomerID(),
                        controller.getMasking());
            } catch (Exception e) {
                logger.debug("speakeasy-sdk: Failed to ingest request:", e);
            }
        }
    }
}
