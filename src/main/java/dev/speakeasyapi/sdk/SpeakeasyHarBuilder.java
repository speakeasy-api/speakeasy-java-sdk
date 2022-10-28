package dev.speakeasyapi.sdk;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

public class SpeakeasyHarBuilder {
    private final String sdkName = "speakeasy-java-sdk";
    private final String speakeasyVersion = "1.3.0";
    private final String droppedBodyText = "--dropped--";

    private final DefaultHarStreamWriter.Builder harWriterBuilder;

    private final Logger logger;

    public SpeakeasyHarBuilder(Logger logger) {
        this.harWriterBuilder = new DefaultHarStreamWriter.Builder();
        this.logger = logger;
    }

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

    private HarRequest harRequest;

    public HarRequest getHarRequest() {
        return this.harRequest;
    }

    private HarResponse harResponse;

    public HarResponse getHarResponse() {
        return this.harResponse;
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

    public SpeakeasyHarBuilder withRequest(SpeakeasyRequest request) throws IOException {
        // Parse cookies
        List<HarCookie> harCookieList = new ArrayList<>();

        List<SpeakeasyCookie> cookies = request.getCookies();

        if (cookies != null) {
            harCookieList = cookies.stream().map(c -> {
                HarCookieBuilder cookieBuilder = new HarCookieBuilder();
                try {
                    cookieBuilder.withName(c.getName())
                            .withValue(c.getValue());
                } catch (Exception e) {
                    this.logger.debug("speakeasy-sdk, error building cookies:", e);
                }
                return cookieBuilder.build();
            }).collect(Collectors.toList());
        }

        // Parse headers
        List<HarHeader> harHeaderList = new ArrayList<HarHeader>();

        Map<String, List<String>> headers = request.getHeaders();

        if (headers != null) {
            harHeaderList = headers.entrySet()
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
                .withMimeType(request.getContentType())
                .withText(request.getBodyText(droppedBodyText));

        HarPostData postData = postDataBuilder.build();

        String queryString = StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";

        HarRequestBuilder builder = new HarRequestBuilder()
                .withBodySize(request.getContentLength())
                .withCookies(harCookieList)
                .withHeaders(harHeaderList)
                .withHeadersSize(request.getHeaderSize())
                .withHttpVersion(request.getProtocol())
                .withMethod(request.getMethod())
                .withQueryString(queryString)
                .withUrl(request.getRequestURI());

        if (request.getContentLength() > 0) {
            builder.withPostData(postData);
        }

        this.harRequest = builder.build();
        return this;
    }

    public SpeakeasyHarBuilder withResponse(SpeakeasyResponse response, String httpVersion) {
        if (response == null) {
            return this;
        }

        // Parse headers
        List<HarHeader> harHeaderList = null;

        Map<String, List<String>> headers = response.getHeaders();

        if (headers != null) {
            harHeaderList = headers.entrySet()
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

        // Parse cookies
        List<SpeakeasyCookie> cookies = response.getCookies(this.startTime);
        List<HarCookie> harCookieList = null;

        if (cookies != null) {
            harCookieList = cookies.stream().map(c -> {
                HarCookieBuilder cookieBuilder = new HarCookieBuilder();
                try {
                    cookieBuilder
                            .withName(c.getName())
                            .withValue(c.getValue())
                            .withExpires(c.getExpires())
                            .withHttpOnly(c.getHttpOnly())
                            .withPath(c.getPath())
                            .withSecure(c.getSecure())
                            .withDomain(c.getDomain());
                } catch (Exception e) {
                    this.logger.debug("speakeasy-sdk, error building cookies:", e);
                }
                return cookieBuilder.build();
            }).collect(Collectors.toList());
        }

        String contentType = response.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream"; // Default HTTP content type
        }

        HarContentBuilder harContentBuilder = new HarContentBuilder()
                .withSize(response.getContentLength(false))
                .withMimeType(contentType);

        long bodySize = response.getContentLength(true);
        if (response.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) {
            bodySize = 0;
            harContentBuilder.withSize(-1l);
        } else {
            harContentBuilder.withText(response.getBodyText(droppedBodyText));
        }

        String redirectURL = response.getLocationHeader();

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
                .withHeadersSize(response.getHeaderSize())
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

    public void build() throws IOException {
        DefaultHarStreamWriter harWriter = harWriterBuilder
                .withOutputStream(outputStream)
                .withComment(comment)
                .withCreator(new HarCreator(this.sdkName, "", this.speakeasyVersion))
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
}
