package dev.speakeasyapi.springboot;

import java.util.concurrent.CountDownLatch;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;

class TestSpeakeasyClient implements ISpeakeasyClient {
    public String HarString;
    public String PathHint;
    public String CustomerID;
    public CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void ingestGrpc(String harString, String pathHint, String customerID) {
        this.HarString = harString;
        this.PathHint = pathHint;
        this.CustomerID = customerID;

        latch.countDown();
    }

    @Override
    public Embedaccesstoken.EmbedAccessTokenResponse getEmbedAccessToken(
            Embedaccesstoken.EmbedAccessTokenRequest request) throws RuntimeException {
        return null;
    }
}