package dev.speakeasyapi.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.speakeasyapi.sdk.models.SpeakeasyAccessTokenFilterBuilder;
import dev.speakeasyapi.sdk.models.SpeakeasyAccessTokenFilterKey;
import dev.speakeasyapi.sdk.models.SpeakeasyAccessTokenFilterOperator;

@DisplayName("SpeakeasyEmbedAccessTokenRequestBuilderTest")
public class SpeakeasyAccessTokenFilterBuilderTest {
    private SpeakeasyAccessTokenFilterBuilder builder;

    @BeforeEach
    public void setup() {
        builder = new SpeakeasyAccessTokenFilterBuilder();
    }

    @Test
    public void appliesSingleFilter() {
        this.builder.withFilter(SpeakeasyAccessTokenFilterKey.CustomerId,
                SpeakeasyAccessTokenFilterOperator.Equality,
                "test-customer");

        assertEquals("customer_id:=:test-customer", this.builder.toString());
    }

    @Test
    public void appliesGreaterLessThanFilters() {
        this.builder.withGreaterThanFilter(SpeakeasyAccessTokenFilterKey.Created, "yesterday");
        this.builder.withLessThanFilter(SpeakeasyAccessTokenFilterKey.Created, "today");

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
        this.builder.withTimeFilter(time, SpeakeasyAccessTokenFilterOperator.LessThan);

        assertEquals(String.format("created_at:<:%s", time), this.builder.toString());
    }
}
