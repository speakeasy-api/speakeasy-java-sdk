package dev.speakeasyapi.sdk.client;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import javax.net.ssl.SSLException;

import dev.speakeasyapi.accesstokens.EmbedAccessTokenServiceGrpc;
import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import dev.speakeasyapi.schemas.Ingest;
import dev.speakeasyapi.schemas.Ingest.IngestRequest.MaskingMetadata;
import dev.speakeasyapi.schemas.IngestServiceGrpc;
import dev.speakeasyapi.sdk.SpeakeasyConfig;
import dev.speakeasyapi.sdk.masking.Masking;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;

public class SpeakeasyClient implements ISpeakeasyClient {
    private final String authKey = "x-api-key";
    private final Metadata.Key<String> metadataKey = Metadata.Key.of(authKey, ASCII_STRING_MARSHALLER);
    private final SpeakeasyConfig cfg;

    public SpeakeasyClient(SpeakeasyConfig cfg) throws IllegalArgumentException {
        cfg.validate();
        this.cfg = cfg;
    }

    @Override
    public void ingestGrpc(String harString, String pathHint, String customerID, Masking masking)
            throws RuntimeException {
        if (!this.cfg.isIngestEnabled()) {
            return;
        }

        ManagedChannel channel = null;

        try {
            channel = createChannel();
        } catch (SSLException e) {
            throw new RuntimeException("Failed to create channel", e);
        }

        try {
            IngestServiceGrpc.IngestServiceBlockingStub blockingStub = IngestServiceGrpc.newBlockingStub(channel);

            MaskingMetadata maskingMetadata = MaskingMetadata.newBuilder()
                    .putAllQueryStringMasks(masking.getQueryStringMasks())
                    .putAllRequestHeaderMasks(masking.getRequestHeaderMasks())
                    .putAllResponseHeaderMasks(masking.getResponseHeaderMasks())
                    .putAllRequestCookieMasks(masking.getRequestCookieMasks())
                    .putAllResponseCookieMasks(masking.getResponseCookieMasks())
                    .putAllRequestFieldMasksString(masking.getRequestBodyMasksString())
                    .putAllRequestFieldMasksNumber(masking.getRequestBodyMasksNumber())
                    .putAllResponseFieldMasksString(masking.getResponseBodyMasksString())
                    .putAllResponseFieldMasksNumber(masking.getResponseBodyMasksNumber())
                    .build();

            Ingest.IngestRequest.Builder ingestRequestBuilder = Ingest.IngestRequest.newBuilder()
                    .setHar(harString)
                    .setApiId(this.cfg.getApiID())
                    .setVersionId(this.cfg.getVersionID())
                    .setMaskingMetadata(maskingMetadata)
                    .setPathHint(pathHint);
            if (customerID != null) {
                ingestRequestBuilder.setCustomerId(customerID);
            }

            Ingest.IngestRequest ingestRequest = ingestRequestBuilder.build();
            blockingStub.ingest(ingestRequest);

            channel.shutdown();
            channel.awaitTermination(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            channel.shutdown();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Embedaccesstoken.EmbedAccessTokenResponse getEmbedAccessToken(
            Embedaccesstoken.EmbedAccessTokenRequest request) throws RuntimeException {
        ManagedChannel channel;
        try {
            channel = createChannel();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
        EmbedAccessTokenServiceGrpc.EmbedAccessTokenServiceBlockingStub blockingStub = EmbedAccessTokenServiceGrpc
                .newBlockingStub(channel);
        return blockingStub.get(request);
    }

    private ManagedChannel createChannel() throws SSLException {
        Metadata metadata = new Metadata();
        metadata.put(metadataKey, this.cfg.getApiKey());

        final String serverUrl = this.cfg.getServerUrl();

        ManagedChannelBuilder<?> channelBuilder;
        if (this.cfg.isSecureGrpc()) {
            channelBuilder = NettyChannelBuilder.forTarget(serverUrl)
                    .sslContext(GrpcSslContexts.forClient()
                            .build());
        } else {
            channelBuilder = ManagedChannelBuilder
                    .forTarget(serverUrl)
                    .usePlaintext();
        }

        return channelBuilder
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build();
    }
}
