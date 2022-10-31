package dev.speakeasyapi.sdk.models;

public enum SpeakeasyAccessTokenFilterOperator {
    Equality("="),
    GreaterThan(">"),
    LessThan("<");

    private String value;

    SpeakeasyAccessTokenFilterOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
