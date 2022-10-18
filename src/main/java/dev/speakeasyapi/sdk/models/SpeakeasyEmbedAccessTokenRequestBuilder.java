package dev.speakeasyapi.sdk.models;

import java.time.Instant;
import java.util.stream.Collectors;

import dev.speakeasyapi.accesstokens.Embedaccesstoken;

public class SpeakeasyEmbedAccessTokenRequestBuilder {
    private Embedaccesstoken.EmbedAccessTokenRequest.Builder requestBuilder = Embedaccesstoken.EmbedAccessTokenRequest
            .newBuilder();

    public SpeakeasyEmbedAccessTokenRequestBuilder withFilter(String key, String operator, String value) {
        requestBuilder.addFilters(Embedaccesstoken.EmbedAccessTokenRequest.Filter.newBuilder()
                .setKey(key)
                .setOperator(operator)
                .setValue(value)
                .build());
        return this;
    }

    public SpeakeasyEmbedAccessTokenRequestBuilder withFilter(SpeakeasyEmbedAccessTokenRequestFilterKey key,
            SpeakeasyEmbedAccessTokenRequestFilterOperator operator,
            String value) {
        return this.withFilter(key.toString(), operator.toString(), value);
    }

    public SpeakeasyEmbedAccessTokenRequestBuilder withGreaterThanFilter(SpeakeasyEmbedAccessTokenRequestFilterKey key,
            String value) {
        return this.withFilter(key, SpeakeasyEmbedAccessTokenRequestFilterOperator.GreaterThan, value);
    }

    public SpeakeasyEmbedAccessTokenRequestBuilder withLessThanFilter(SpeakeasyEmbedAccessTokenRequestFilterKey key,
            String value) {
        return this.withFilter(key, SpeakeasyEmbedAccessTokenRequestFilterOperator.LessThan, value);
    }

    public SpeakeasyEmbedAccessTokenRequestBuilder withEqualityFilter(SpeakeasyEmbedAccessTokenRequestFilterKey key,
            String value) {
        return this.withFilter(key, SpeakeasyEmbedAccessTokenRequestFilterOperator.Equality, value);
    }

    public SpeakeasyEmbedAccessTokenRequestBuilder withCustomerIdFilter(String customerId) {
        return this.withEqualityFilter(SpeakeasyEmbedAccessTokenRequestFilterKey.CustomerId, customerId);
    }

    public SpeakeasyEmbedAccessTokenRequestBuilder withTimeFilter(Instant time,
            SpeakeasyEmbedAccessTokenRequestFilterOperator operator) {
        // Instant.toString() is ISO-8601 format
        return this.withFilter(SpeakeasyEmbedAccessTokenRequestFilterKey.Created, operator,
                time.toString());
    }

    public Embedaccesstoken.EmbedAccessTokenRequest build() {
        return requestBuilder.build();
    }

    @Override
    public String toString() {
        return this.requestBuilder.getFiltersList()
                .stream()
                .map(f -> String.format("%s:%s:%s", f.getKey(), f.getOperator(), f.getValue()))
                .collect(Collectors.joining(";"));

    }
}
