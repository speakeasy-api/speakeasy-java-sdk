package dev.speakeasyapi.sdk;

import java.util.List;
import java.util.Map;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;
import dev.speakeasyapi.accesstokens.Embedaccesstoken.EmbedAccessTokenRequest.Filter;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;
import dev.speakeasyapi.sdk.masking.Masking;

public class SpeakeasyMiddlewareController {
    public static final String Key = "speakeasyMiddlewareController";

    private final ISpeakeasyClient client;
    private String pathHint = "";
    private String customerID = "";
    private boolean enabled = false;
    private Masking masking = Masking.builder().build();

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

    public void setMasking(Masking masking) {
        this.masking = masking;
    }

    public Masking getMasking() {
        return masking;
    }

    public String getEmbedAccessToken(List<Filter> filters) throws RuntimeException {
        Embedaccesstoken.EmbedAccessTokenRequest request = Embedaccesstoken.EmbedAccessTokenRequest
                .newBuilder().addAllFilters(filters).build();

        return client.getEmbedAccessToken(request).getAccessToken();
    }

    public String getPortalLoginToken(String customerId, String displayName, Map<String, String> jwtCustomClaims,
            List<String> permissions, List<Filter> filters) throws RuntimeException {
        Embedaccesstoken.EmbedAccessTokenRequest request = Embedaccesstoken.EmbedAccessTokenRequest
                .newBuilder()
                .addAllFilters(filters)
                .setCustomerId(customerId)
                .setDisplayName(displayName)
                .putAllJwtCustomClaims(jwtCustomClaims)
                .addAllPermissions(permissions)
                .build();

        return client.getEmbedAccessToken(request).getAccessToken();
    }
}
