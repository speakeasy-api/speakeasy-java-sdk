package dev.speakeasyapi.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.speakeasyapi.sdk.masking.Masking;

@DisplayName("MaskingTest")
public class MaskingTest {
    @BeforeEach
    public void setup() {
    }

    @Test
    public void replacesStringFieldInBody() {
        Masking masking = Masking.builder().maskRequestBodyStrings(Arrays.asList("test")).build();
        String masked = masking.maskRequestBody("{\"test\": \"hello\"}", "application/json; charset=utf-8");
        assertEquals("{\"test\": \"__masked__\"}", masked);
    }
}
