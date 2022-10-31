package dev.speakeasyapi.sdk.client;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import dev.speakeasyapi.sdk.masking.Masking;

public interface ISpeakeasyClient {
        void ingestGrpc(String harString, String pathHint, String customerID, Masking masking)
                        throws RuntimeException;

        Embedaccesstoken.EmbedAccessTokenResponse getEmbedAccessToken(Embedaccesstoken.EmbedAccessTokenRequest request)
                        throws RuntimeException;
}
