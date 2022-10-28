package dev.speakeasyapi.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.speakeasyapi.sdk.models.SpeakeasyEmbedAccessTokenRequestBuilder;
import dev.speakeasyapi.sdk.models.SpeakeasyEmbedAccessTokenRequestFilterKey;
import dev.speakeasyapi.sdk.models.SpeakeasyEmbedAccessTokenRequestFilterOperator;

@DisplayName("SpeakeasyEmbedAccessTokenRequestBuilderTest")
public class SpeakeasyEmbedAccessTokenRequestBuilderTest {
    private SpeakeasyEmbedAccessTokenRequestBuilder builder;

    @BeforeEach
    public void setup() {
        builder = new SpeakeasyEmbedAccessTokenRequestBuilder();
    }

    @Test
    public void appliesSingleFilter() {
        this.builder.withFilter(SpeakeasyEmbedAccessTokenRequestFilterKey.CustomerId,
                SpeakeasyEmbedAccessTokenRequestFilterOperator.Equality,
                "test-customer");

        assertEquals("customer_id:=:test-customer", this.builder.toString());
    }

    @Test
    public void appliesGreaterLessThanFilters() {
        this.builder.withGreaterThanFilter(SpeakeasyEmbedAccessTokenRequestFilterKey.Created, "yesterday");
        this.builder.withLessThanFilter(SpeakeasyEmbedAccessTokenRequestFilterKey.Created, "today");

        assertEquals("created_at:>:yesterday;created_at:<:today", this.builder.toString());
    }

    @Test
    public void appliesCustomerFilter() {
        this.builder.withCustomerIdFilter("test-customer");

        assertEquals("customer_id:=:test-customer", this.builder.toString());
    }

    @Test
    public void appliesTimeFilter() {
        Instant time = Instant.now();
        this.builder.withTimeFilter(time, SpeakeasyEmbedAccessTokenRequestFilterOperator.LessThan);

        assertEquals(String.format("created_at:<:%s", time), this.builder.toString());
    }
}
