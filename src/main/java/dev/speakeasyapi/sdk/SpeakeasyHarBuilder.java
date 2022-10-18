package dev.speakeasyapi.sdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.smartbear.har.builder.HarContentBuilder;
import com.smartbear.har.builder.HarCookieBuilder;
import com.smartbear.har.builder.HarEntryBuilder;
import com.smartbear.har.builder.HarHeaderBuilder;
import com.smartbear.har.builder.HarPostDataBuilder;
import com.smartbear.har.builder.HarRequestBuilder;
import com.smartbear.har.builder.HarResponseBuilder;
import com.smartbear.har.creator.DefaultHarStreamWriter;
import com.smartbear.har.model.HarCache;
import com.smartbear.har.model.HarCookie;
import com.smartbear.har.model.HarCreator;
import com.smartbear.har.model.HarHeader;
import com.smartbear.har.model.HarPostData;
import com.smartbear.har.model.HarRequest;
import com.smartbear.har.model.HarResponse;
import com.smartbear.har.model.HarTimings;

import dev.speakeasyapi.springboot.SpeakeasyResponseWrapper;

public class SpeakeasyHarBuilder {
    private final String droppedBodyText = "--dropped--";
    private final String cookieResponseHeaderName = "Set-Cookie";

    private final DefaultHarStreamWriter.Builder harWriterBuilder;

    private OutputStream outputStream;

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    private String comment;

    public String getComment() {
        return this.comment;
    }

    private Instant startTime = Instant.now();

    public Instant getStartTime() {
        return this.startTime;
    }

    private Instant endTime = Instant.now();

    public Instant getEndTime() {
        return this.endTime;
    }

    private String hostName;

    public String getHostName() {
        return this.hostName;
    }

    private String port;

    public String getPort() {
        return this.port;
    }

    private HarCreator creator;

    public HarCreator getCreator() {
        return this.creator;
    }

    private HarRequest harRequest;

    public HarRequest getHarRequest() {
        return this.harRequest;
    }

    private HarResponse harResponse;

    public HarResponse getHarResponse() {
        return this.harResponse;
    }

    public SpeakeasyHarBuilder() {
        this.harWriterBuilder = new DefaultHarStreamWriter.Builder();
    }

    public SpeakeasyHarBuilder withStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public SpeakeasyHarBuilder withEndTime(Instant endTime) {
        this.endTime = endTime;
        return this;
    }

    public SpeakeasyHarBuilder withHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public SpeakeasyHarBuilder withPort(int port) {
        this.port = String.valueOf(port);
        return this;
    }

    private BinaryOperator<List<String>> merge = new BinaryOperator<List<String>>() {
        @Override
        public List<String> apply(List<String> o, List<String> o2) {
            o.addAll(o2);
            return o;
        }
    };

    public SpeakeasyHarBuilder withRequest(HttpServletRequest request,
            ByteArrayOutputStream requestOutputStream,
            boolean captureRequest,
            Logger logger)
            throws IOException {
        Map<String, List<String>> headerMap = null;
        if (request.getHeaderNames() != null) {
            headerMap = Collections.list(request.getHeaderNames()).stream()
                    .collect(Collectors.toMap(h -> h, h -> Collections.list(request.getHeaders(h)), merge));
        }

        List<HarCookie> harCookieList = new ArrayList<>();
        // Note: It appears that java inserts cookies with headers,
        // so this never gets called.
        if (request.getCookies() != null) {
            // Parse cookies
            harCookieList = Arrays.stream(request.getCookies()).map(c -> {
                HarCookieBuilder cookieBuilder = new HarCookieBuilder();
                try {
                    cookieBuilder.withName(c.getName())
                            .withValue(c.getValue());
                } catch (Exception e) {
                    logger.debug("speakeasy-sdk, error building cookies:", e);
                }
                return cookieBuilder.build();
            }).collect(Collectors.toList());
        }

        // Parse headers
        List<HarHeader> harHeaderList = new ArrayList<HarHeader>();

        if (headerMap != null) {
            harHeaderList = headerMap.entrySet()
                    .stream()
                    .map(entry -> {
                        List<HarHeader> headersList = new ArrayList<>();
                        for (String value : entry.getValue()) {
                            headersList.add(new HarHeaderBuilder()
                                    .withName(entry.getKey())
                                    .withValue(value)
                                    .build());
                        }
                        return headersList;
                    }).flatMap(list -> list.stream())
                    .collect(Collectors.toList());

            harHeaderList.sort((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));
        }

        HarPostDataBuilder postDataBuilder = new HarPostDataBuilder()
                .withMimeType(request.getContentType());
        if (captureRequest) {
            postDataBuilder.withText(requestOutputStream.toString());
        } else {
            postDataBuilder.withText(droppedBodyText);
        }
        HarPostData postData = postDataBuilder.build();

        String queryString = StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";

        HarRequestBuilder builder = new HarRequestBuilder()
                .withBodySize(request.getContentLengthLong())
                .withCookies(harCookieList)
                .withHeaders(harHeaderList)
                .withHeadersSize(calculateHeaderSize(headerMap))
                .withHttpVersion(request.getProtocol())
                .withMethod(request.getMethod())
                .withQueryString(queryString)
                .withUrl(request.getRequestURI());

        if (requestOutputStream.size() > 0) {
            builder.withPostData(postData);
        }

        this.harRequest = builder.build();
        return this;
    }

    public SpeakeasyHarBuilder withResponse(HttpServletResponse response,
            ByteArrayOutputStream responseOutputStream,
            boolean captureResponse,
            String httpVersion,
            Logger logger) {
        if (response == null) {
            return this;
        }
        Map<String, List<String>> headerMap = null;
        List<HarCookie> harCookieList = null;
        List<HarHeader> harHeaderList = null;

        if (response.getHeaderNames() != null) {
            headerMap = response.getHeaderNames().stream()
                    .collect(Collectors.toMap(h -> h, h -> new ArrayList<>(response.getHeaders(h)), merge));

            // Parse cookies
            harCookieList = headerMap.entrySet()
                    .stream()
                    .filter(entry -> cookieResponseHeaderName.equals(entry.getKey()))
                    .flatMap(entry -> entry.getValue().stream().map(hv -> parseSetCookieString(hv)))
                    .collect(Collectors.toList());

            // Parse headers
            harHeaderList = headerMap.entrySet()
                    .stream()
                    .map(entry -> {
                        List<HarHeader> headersList = new ArrayList<>();
                        for (String value : entry.getValue()) {
                            headersList.add(new HarHeaderBuilder()
                                    .withName(entry.getKey())
                                    .withValue(value)
                                    .build());
                        }
                        return headersList;
                    }).flatMap(list -> list.stream())
                    .collect(Collectors.toList());

            harHeaderList.sort((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));

        }

        String contentType = response.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream"; // Default HTTP content type
        }

        try {
            responseOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long contentSize = ((SpeakeasyResponseWrapper) response).getRealBodySize();

        HarContentBuilder harContentBuilder = new HarContentBuilder()
                .withSize(captureResponse && contentSize > 0 ? contentSize : -1)
                .withMimeType(contentType);

        long bodySize = contentSize > 0 ? contentSize : -1;
        if (response.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) {
            bodySize = 0;
            harContentBuilder.withSize(-1l);
        } else {
            if (captureResponse) {
                harContentBuilder.withText(responseOutputStream.toString());
            } else {
                harContentBuilder.withText(droppedBodyText);
            }
        }

        String redirectURL = "";
        Collection<String> locationHeaders = response.getHeaders("Location");
        if (locationHeaders != null && locationHeaders.size() > 0) {
            redirectURL = locationHeaders.iterator().next();
        }

        int statusCode = response.getStatus();
        String statusText = null;
        try {
            statusText = HttpStatus.valueOf(statusCode).getReasonPhrase();
        } catch (Exception e) {
            logger.debug("speakeasy-sdk, error retrieving status: ", e);
        }
        this.harResponse = new HarResponseBuilder()
                .withStatus(statusCode)
                .withStatusText(statusText)
                .withCookies(harCookieList)
                .withContent(harContentBuilder.build())
                .withBodySize(bodySize)
                .withHeaders(harHeaderList)
                .withHeadersSize(calculateHeaderSize(headerMap))
                .withHttpVersion(httpVersion)
                .withRedirectURL(redirectURL)
                .build();

        return this;
    }

    public SpeakeasyHarBuilder withOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    public SpeakeasyHarBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public SpeakeasyHarBuilder withCreator(HarCreator creator) {
        this.creator = creator;
        return this;
    }

    private HarCookie parseSetCookieString(String cookie) {
        HarCookieBuilder cookieBuilder = new HarCookieBuilder();

        String[] cookieParts = cookie.split("; ");

        for (String cookiePart : cookieParts) {
            String cookieRegex = "([^=]+)=?([^=]*)";
            Pattern pattern = Pattern.compile(cookieRegex);
            Matcher matcher = pattern.matcher(cookiePart);

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                switch (key.toLowerCase()) {
                    case "domain":
                        cookieBuilder.withDomain(value);
                        break;
                    case "max-age": {
                        int maxAge = Integer.parseInt(value);

                        Instant expires = startTime.plus(maxAge, ChronoUnit.SECONDS);

                        cookieBuilder.withExpires(expires.toString());
                        break;
                    }
                    case "expires": {
                        try {
                            SimpleDateFormat inFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                            Date expiresDate = inFormat.parse(value);

                            SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                            outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            String expires = outFormat.format(expiresDate);

                            cookieBuilder.withExpires(expires);
                        } catch (Exception e) {
                        }
                        break;
                    }
                    case "httponly":
                        cookieBuilder.withHttpOnly(true);
                        break;
                    case "path":
                        cookieBuilder.withPath(value);
                        break;
                    case "secure":
                        cookieBuilder.withSecure(true);
                        break;
                    default:
                        cookieBuilder.withName(key);
                        cookieBuilder.withValue(value);
                        break;
                }
            }
        }
        return cookieBuilder.build();
    }

    public void build() throws IOException {
        DefaultHarStreamWriter harWriter = harWriterBuilder
                .withOutputStream(outputStream)
                .withComment(comment)
                .withCreator(creator)
                .build();

        HarEntryBuilder builder = new HarEntryBuilder()
                .withRequest(harRequest)
                .withResponse(harResponse)
                .withServerIPAddress(hostName)
                .withStartedDateTime(startTime)
                .withTime(endTime.toEpochMilli() - startTime.toEpochMilli())
                .withCache(new HarCache())
                .withTimings(new HarTimings(0l, 0l, 0l, -1l, -1l, -1l, 0l, ""));

        if (!port.equals("-1")) {
            builder.withConnection(port);
        }

        harWriter.addEntry(builder.build());
        harWriter.closeHar();
    }

    private long calculateHeaderSize(Map<String, List<String>> headers) {
        if (headers == null) {
            return 0;
        }

        StringBuilder builder = new StringBuilder();
        headers.forEach((key, values) -> {
            for (String value : values) {
                builder.append(key)
                        .append(": ")
                        .append(value)
                        .append("\r\n");
            }
        });

        return builder.toString().getBytes().length;
    }
}
