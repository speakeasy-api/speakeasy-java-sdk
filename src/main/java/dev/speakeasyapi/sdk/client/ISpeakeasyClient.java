package dev.speakeasyapi.sdk.client;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;

public interface ISpeakeasyClient {
    void ingestGrpc(String harString, String pathHint, String customerID)
            throws RuntimeException;

    Embedaccesstoken.EmbedAccessTokenResponse getEmbedAccessToken(Embedaccesstoken.EmbedAccessTokenRequest request)
            throws RuntimeException;
}
