package dev.speakeasyapi.sdk.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.speakeasyapi.accesstokens.Embedaccesstoken.EmbedAccessTokenRequest.Filter;

public class SpeakeasyAccessTokenFilterBuilder {
    private List<Filter> filters = new ArrayList<>();

    public SpeakeasyAccessTokenFilterBuilder withFilter(String key, String operator, String value) {
        filters.add(Filter.newBuilder().setKey(key).setOperator(operator).setValue(value).build());
        return this;
    }

    public SpeakeasyAccessTokenFilterBuilder withFilter(SpeakeasyAccessTokenFilterKey key,
            SpeakeasyAccessTokenFilterOperator operator,
            String value) {
        return this.withFilter(key.toString(), operator.toString(), value);
    }

    public SpeakeasyAccessTokenFilterBuilder withGreaterThanFilter(SpeakeasyAccessTokenFilterKey key,
            String value) {
        return this.withFilter(key, SpeakeasyAccessTokenFilterOperator.GreaterThan, value);
    }

    public SpeakeasyAccessTokenFilterBuilder withLessThanFilter(SpeakeasyAccessTokenFilterKey key,
            String value) {
        return this.withFilter(key, SpeakeasyAccessTokenFilterOperator.LessThan, value);
    }

    public SpeakeasyAccessTokenFilterBuilder withEqualityFilter(SpeakeasyAccessTokenFilterKey key,
            String value) {
        return this.withFilter(key, SpeakeasyAccessTokenFilterOperator.Equality, value);
    }

    public SpeakeasyAccessTokenFilterBuilder withCustomerIdFilter(String customerId) {
        return this.withEqualityFilter(SpeakeasyAccessTokenFilterKey.CustomerId, customerId);
    }

    public SpeakeasyAccessTokenFilterBuilder withTimeFilter(Instant time,
            SpeakeasyAccessTokenFilterOperator operator) {
        // Instant.toString() is ISO-8601 format
        return this.withFilter(SpeakeasyAccessTokenFilterKey.Created, operator,
                time.toString());
    }

    public List<Filter> build() {
        return filters;
    }

    @Override
    public String toString() {
        return filters
                .stream()
                .map(f -> String.format("%s:%s:%s", f.getKey(), f.getOperator(), f.getValue()))
                .collect(Collectors.joining(";"));
    }
}
