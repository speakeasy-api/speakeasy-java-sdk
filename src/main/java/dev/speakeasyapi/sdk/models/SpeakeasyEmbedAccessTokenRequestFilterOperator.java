package dev.speakeasyapi.sdk.models;

public enum SpeakeasyEmbedAccessTokenRequestFilterOperator {
    Equality("="),
    GreaterThan(">"),
    LessThan("<");

    private String value;

    SpeakeasyEmbedAccessTokenRequestFilterOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
