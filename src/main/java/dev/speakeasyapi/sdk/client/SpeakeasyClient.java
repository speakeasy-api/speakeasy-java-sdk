package dev.speakeasyapi.sdk.client;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

import dev.speakeasyapi.accesstokens.EmbedAccessTokenServiceGrpc;
import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import org.springframework.util.StringUtils;

import dev.speakeasyapi.schemas.Ingest;
import dev.speakeasyapi.schemas.IngestServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;

public class SpeakeasyClient implements ISpeakeasyClient {
    private final String authKey = "x-api-key";
    private final Metadata.Key<String> metadataKey = Metadata.Key.of(authKey, ASCII_STRING_MARSHALLER);
    private final String apiKey;
    private final String apiID;
    private final String versionID;
    private final String serverUrl;
    private final boolean secureGrpc;
    private final boolean disableIngest;

    public SpeakeasyClient(String apiKey, String apiID, String versionID, String serverUrl, boolean secureGrpc, boolean disableIngest)
            throws IllegalArgumentException {
        validate(apiKey, apiID, versionID);

        this.apiKey = apiKey;
        this.apiID = apiID;
        this.versionID = versionID;
        this.serverUrl = serverUrl;
        this.secureGrpc = secureGrpc;
        this.disableIngest = disableIngest;
    }

    private void validate(String apiKey, String apiID, String versionID) throws IllegalArgumentException {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException("Speakeasy API key is required.");
        }

        int maxIDSize = 128;
        String validCharsRegexStr = "[^a-zA-Z0-9.\\-_~]";

        if (!StringUtils.hasText(apiID)) {
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

        if (!StringUtils.hasText(versionID)) {
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

    @Override
    public void ingestGrpc(String harString, String pathHint, String customerID)
            throws RuntimeException {
        if (this.disableIngest) {
            return;
        }

        try {
            ManagedChannel channel = createChannel();

            IngestServiceGrpc.IngestServiceBlockingStub blockingStub = IngestServiceGrpc.newBlockingStub(channel);

            Ingest.IngestRequest.Builder ingestRequestBuilder = Ingest.IngestRequest.newBuilder()
                    .setHar(harString)
                    .setApiId(this.apiID)
                    .setVersionId(this.versionID)
                    .setPathHint(pathHint);
            if (customerID != null) {
                ingestRequestBuilder.setCustomerId(customerID);
            }

            Ingest.IngestRequest ingestRequest = ingestRequestBuilder.build();
            blockingStub.ingest(ingestRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Embedaccesstoken.EmbedAccessTokenResponse getEmbedAccessToken(Embedaccesstoken.EmbedAccessTokenRequest request) throws RuntimeException {
        ManagedChannel channel;
        try {
            channel = createChannel();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
        EmbedAccessTokenServiceGrpc.EmbedAccessTokenServiceBlockingStub blockingStub = EmbedAccessTokenServiceGrpc.newBlockingStub(channel);
        return blockingStub.get(request);
    }

    private ManagedChannel createChannel() throws SSLException {
        Metadata metadata = new Metadata();
        metadata.put(metadataKey, apiKey);

        ManagedChannelBuilder channelBuilder;
        if (secureGrpc) {
            channelBuilder = NettyChannelBuilder.forTarget(this.serverUrl)
                    .sslContext(GrpcSslContexts.forClient()
                            .build());
        } else {
            channelBuilder = ManagedChannelBuilder
                    .forTarget(this.serverUrl)
                    .usePlaintext();
        }

        return channelBuilder
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build();
    }
}
