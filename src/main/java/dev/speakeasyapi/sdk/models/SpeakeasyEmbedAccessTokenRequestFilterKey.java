package dev.speakeasyapi.sdk.models;

public enum SpeakeasyEmbedAccessTokenRequestFilterKey {
    Created("created_at"),
    CustomerId("customer_id");

    private String value;

    SpeakeasyEmbedAccessTokenRequestFilterKey(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
