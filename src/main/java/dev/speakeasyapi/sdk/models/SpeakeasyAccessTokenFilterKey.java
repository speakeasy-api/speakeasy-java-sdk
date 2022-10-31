package dev.speakeasyapi.sdk.models;

public enum SpeakeasyAccessTokenFilterKey {
    Created("created_at"),
    CustomerId("customer_id");

    private String value;

    SpeakeasyAccessTokenFilterKey(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
