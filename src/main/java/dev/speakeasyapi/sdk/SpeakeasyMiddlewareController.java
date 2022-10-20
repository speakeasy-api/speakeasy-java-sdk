package dev.speakeasyapi.sdk;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;

public class SpeakeasyMiddlewareController {
    public static final String ControllerKey = "speakeasyMiddlewareController";

    private String pathHint;
    private String customerID;
    private final ISpeakeasyClient client;

    public SpeakeasyMiddlewareController(ISpeakeasyClient client) {
        this.client = client;
    }

    public void setPathHint(String pathHint) {
        this.pathHint = pathHint;
    }

    public String getPathHint() {
        return pathHint;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getEmbedAccessToken(Embedaccesstoken.EmbedAccessTokenRequest request)
            throws RuntimeException {
        return client.getEmbedAccessToken(request).getAccessToken();
    }
}
