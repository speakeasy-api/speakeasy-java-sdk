package dev.speakeasyapi.sdk;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;

public class SpeakeasyMiddlewareController {
    public static final String Key = "speakeasyMiddlewareController";

    private final ISpeakeasyClient client;
    private String pathHint = "";
    private String customerID = "";
    private boolean enabled = false;

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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEmbedAccessToken(Embedaccesstoken.EmbedAccessTokenRequest request)
            throws RuntimeException {
        return client.getEmbedAccessToken(request).getAccessToken();
    }
}
