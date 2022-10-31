package dev.speakeasyapi.sdk.masking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Masking {
    private static final String DEFAULT_STRING_MASK = "__masked__";
    private static final String DEFAULT_NUMBER_MASK = "-12321";

    private final String stringFieldMatchRegex = "(\"%s\": *)(\".*?[^\\\\]\")( *[, \\n\\r}]?)";
    private final String numberFieldMatchRegex = "(\"%s\": *)(-?[0-9]+\\.?[0-9]*)( *[, \\n\\r}]?)";

    private Map<String, String> queryStringMasks = new HashMap<>();
    private Map<String, String> requestHeaderMasks = new HashMap<>();
    private Map<String, String> responseHeaderMasks = new HashMap<>();
    private Map<String, String> requestCookieMasks = new HashMap<>();
    private Map<String, String> responseCookieMasks = new HashMap<>();
    private Map<String, String> requestBodyMasksString = new HashMap<>();
    private Map<String, String> responseBodyMasksString = new HashMap<>();
    private Map<String, String> requestBodyMasksNumber = new HashMap<>();
    private Map<String, String> responseBodyMasksNumber = new HashMap<>();

    public static class Builder {
        private Masking masking = new Masking();

        public Masking build() {
            return masking;
        }

        public Builder maskQueryStrings(List<String> queryParamNames) {
            for (String queryParamName : queryParamNames) {
                masking.queryStringMasks.put(queryParamName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskQueryStrings(List<String> queryParamNames, String mask) {
            for (String queryParamName : queryParamNames) {
                masking.queryStringMasks.put(queryParamName, mask);
            }

            return this;
        }

        public Builder maskQueryStrings(Map<String, String> queryParamMasks) {
            masking.queryStringMasks.putAll(queryParamMasks);

            return this;
        }

        public Builder maskRequestHeaders(List<String> headerNames) {
            for (String headerName : headerNames) {
                masking.requestHeaderMasks.put(headerName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskRequestHeaders(List<String> headerNames, String mask) {
            for (String headerName : headerNames) {
                masking.requestHeaderMasks.put(headerName, mask);
            }

            return this;
        }

        public Builder maskRequestHeaders(Map<String, String> headerMasks) {
            masking.requestHeaderMasks.putAll(headerMasks);

            return this;
        }

        public Builder maskResponseHeaders(List<String> headerNames) {
            for (String headerName : headerNames) {
                masking.responseHeaderMasks.put(headerName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskResponseHeaders(List<String> headerNames, String mask) {
            for (String headerName : headerNames) {
                masking.responseHeaderMasks.put(headerName, mask);
            }

            return this;
        }

        public Builder maskResponseHeaders(Map<String, String> headerMasks) {
            masking.responseHeaderMasks.putAll(headerMasks);

            return this;
        }

        public Builder maskRequestCookies(List<String> cookieNames) {
            for (String cookieName : cookieNames) {
                masking.requestCookieMasks.put(cookieName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskRequestCookies(List<String> cookieNames, String mask) {
            for (String cookieName : cookieNames) {
                masking.requestCookieMasks.put(cookieName, mask);
            }

            return this;
        }

        public Builder maskRequestCookies(Map<String, String> cookieMasks) {
            masking.requestCookieMasks.putAll(cookieMasks);

            return this;
        }

        public Builder maskResponseCookies(List<String> cookieNames) {
            for (String cookieName : cookieNames) {
                masking.responseCookieMasks.put(cookieName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskResponseCookies(List<String> cookieNames, String mask) {
            for (String cookieName : cookieNames) {
                masking.responseCookieMasks.put(cookieName, mask);
            }

            return this;
        }

        public Builder maskResponseCookies(Map<String, String> cookieMasks) {
            masking.responseCookieMasks.putAll(cookieMasks);

            return this;
        }

        public Builder maskRequestBodyStrings(List<String> fieldNames) {
            for (String fieldName : fieldNames) {
                masking.requestBodyMasksString.put(fieldName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskRequestBodyStrings(List<String> fieldNames, String mask) {
            for (String fieldName : fieldNames) {
                masking.requestBodyMasksString.put(fieldName, mask);
            }

            return this;
        }

        public Builder maskRequestBodyStrings(Map<String, String> fieldMasks) {
            masking.requestBodyMasksString.putAll(fieldMasks);

            return this;
        }

        public Builder maskResponseBodyStrings(List<String> fieldNames) {
            for (String fieldName : fieldNames) {
                masking.responseBodyMasksString.put(fieldName, DEFAULT_STRING_MASK);
            }

            return this;
        }

        public Builder maskResponseBodyStrings(List<String> fieldNames, String mask) {
            for (String fieldName : fieldNames) {
                masking.responseBodyMasksString.put(fieldName, mask);
            }

            return this;
        }

        public Builder maskResponseBodyStrings(Map<String, String> fieldMasks) {
            masking.responseBodyMasksString.putAll(fieldMasks);

            return this;
        }

        public Builder maskRequestBodyNumbers(List<String> fieldNames) {
            for (String fieldName : fieldNames) {
                masking.requestBodyMasksNumber.put(fieldName, DEFAULT_NUMBER_MASK);
            }

            return this;
        }

        public Builder maskRequestBodyNumbers(List<String> fieldNames, String mask) {
            for (String fieldName : fieldNames) {
                masking.requestBodyMasksNumber.put(fieldName, mask);
            }

            return this;
        }

        public Builder maskRequestBodyNumbers(Map<String, String> fieldMasks) {
            masking.requestBodyMasksNumber.putAll(fieldMasks);

            return this;
        }

        public Builder maskResponseBodyNumbers(List<String> fieldNames) {
            for (String fieldName : fieldNames) {
                masking.responseBodyMasksNumber.put(fieldName, DEFAULT_NUMBER_MASK);
            }

            return this;
        }

        public Builder maskResponseBodyNumbers(List<String> fieldNames, String mask) {
            for (String fieldName : fieldNames) {
                masking.responseBodyMasksNumber.put(fieldName, mask);
            }

            return this;
        }

        public Builder maskResponseBodyNumbers(Map<String, String> fieldMasks) {
            masking.responseBodyMasksNumber.putAll(fieldMasks);

            return this;
        }

        private Builder() {
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, String> getQueryStringMasks() {
        return queryStringMasks;
    }

    public Map<String, String> getRequestHeaderMasks() {
        return requestHeaderMasks;
    }

    public Map<String, String> getResponseHeaderMasks() {
        return responseHeaderMasks;
    }

    public Map<String, String> getRequestCookieMasks() {
        return requestCookieMasks;
    }

    public Map<String, String> getResponseCookieMasks() {
        return responseCookieMasks;
    }

    public Map<String, String> getRequestBodyMasksString() {
        return requestBodyMasksString;
    }

    public Map<String, String> getRequestBodyMasksNumber() {
        return requestBodyMasksNumber;
    }

    public Map<String, String> getResponseBodyMasksString() {
        return responseBodyMasksString;
    }

    public Map<String, String> getResponseBodyMasksNumber() {
        return responseBodyMasksNumber;
    }

    public String maskRequestBody(String body, String mimeType) {
        return maskBody(body, mimeType, requestBodyMasksString, requestBodyMasksNumber);
    }

    public String maskResponseBody(String body, String mimeType) {
        return maskBody(body, mimeType, responseBodyMasksString, responseBodyMasksNumber);
    }

    private String maskBody(String body, String mimeType, Map<String, String> stringMasks,
            Map<String, String> numberMasks) {
        if (StringUtils.isBlank(mimeType) || !mimeType.toLowerCase().contains("application/json")) {
            return body;
        }

        for (Map.Entry<String, String> entry : stringMasks.entrySet()) {
            Pattern regex = Pattern.compile(String.format(stringFieldMatchRegex,
                    Pattern.quote(entry.getKey())),
                    Pattern.CASE_INSENSITIVE);

            Matcher matcher = regex.matcher(body);

            while (matcher.find()) {
                String match = matcher.group(2);
                int startIdx = matcher.start(2);

                body = body.substring(0, startIdx) + '"' + entry.getValue() + '"'
                        + body.substring(startIdx + match.length());
            }
        }

        for (Map.Entry<String, String> entry : numberMasks.entrySet()) {
            Pattern regex = Pattern.compile(String.format(numberFieldMatchRegex,
                    Pattern.quote(entry.getKey())),
                    Pattern.CASE_INSENSITIVE);

            Matcher matcher = regex.matcher(body);

            while (matcher.find()) {
                String match = matcher.group(2);
                int startIdx = matcher.start(2);

                body = body.substring(0, startIdx) + entry.getValue()
                        + body.substring(startIdx + match.length());
            }
        }

        return body;
    }

    private Masking() {
    }
}
