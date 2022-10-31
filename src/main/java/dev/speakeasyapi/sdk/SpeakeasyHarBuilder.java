package dev.speakeasyapi.sdk;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

import dev.speakeasyapi.sdk.masking.Masking;

public class SpeakeasyHarBuilder {
    private final String sdkName = "speakeasy-java-sdk";
    private final String speakeasyVersion = "1.3.0";
    private final String droppedBodyText = "--dropped--";

    private final DefaultHarStreamWriter.Builder harWriterBuilder;

    private final Logger logger;

    private Masking masking = null;

    public SpeakeasyHarBuilder(Logger logger) {
        this.harWriterBuilder = new DefaultHarStreamWriter.Builder();
        this.logger = logger;
    }

    private OutputStream outputStream;

    public OutputStream getOutputStream() {
        return this.outputStream;
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

    public SpeakeasyHarBuilder withMasking(Masking masking) {
        this.masking = masking;
        return this;
    }

    private SpeakeasyRequest request = null;

    public SpeakeasyHarBuilder withRequest(SpeakeasyRequest request) throws IOException {
        this.request = request;
        // Parse cookies
        List<HarCookie> harCookieList = new ArrayList<>();

        List<SpeakeasyCookie> cookies = request.getCookies();

        if (cookies != null) {
            harCookieList = cookies.stream().map(c -> {
                HarCookieBuilder cookieBuilder = new HarCookieBuilder();
                try {
                    String name = c.getName();
                    String value = c.getValue();

                    if (this.masking != null) {
                        String mask = this.masking.getRequestCookieMasks().get(name);
                        if (mask != null) {
                            value = mask;
                        }
                    }

                    cookieBuilder.withName(name).withValue(value);
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
                            String name = entry.getKey();
                            if (this.masking != null) {
                                String mask = this.masking.getRequestHeaderMasks().get(name);
                                if (mask != null) {
                                    value = mask;
                                }
                            }

                            headersList.add(new HarHeaderBuilder()
                                    .withName(name)
                                    .withValue(value)
                                    .build());
                        }
                        return headersList;
                    }).flatMap(list -> list.stream())
                    .collect(Collectors.toList());

            harHeaderList.sort((h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));
        }

        String body = request.getBodyText(droppedBodyText);
        if (this.masking != null) {
            body = this.masking.maskRequestBody(body, request.getContentType());
        }

        HarPostDataBuilder postDataBuilder = new HarPostDataBuilder()
                .withMimeType(request.getContentType())
                .withText(body);

        HarPostData postData = postDataBuilder.build();

        String url = resolveURL(request);

        HarRequestBuilder builder = new HarRequestBuilder()
                .withBodySize(request.getContentLength())
                .withCookies(harCookieList)
                .withHeaders(harHeaderList)
                .withHeadersSize(request.getHeaderSize())
                .withHttpVersion(request.getProtocol())
                .withMethod(request.getMethod())
                .withQueryString(getMaskedQueryString(request.getQueryString()))
                .withUrl(url);

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
                            String name = entry.getKey();
                            if (this.masking != null) {
                                String mask = this.masking.getResponseHeaderMasks().get(name);
                                if (mask != null) {
                                    value = mask;
                                }
                            }

                            headersList.add(new HarHeaderBuilder()
                                    .withName(name)
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
                    String name = c.getName();
                    String value = c.getValue();

                    if (this.masking != null) {
                        String mask = this.masking.getResponseCookieMasks().get(name);
                        if (mask != null) {
                            value = mask;
                        }
                    }

                    cookieBuilder
                            .withName(name)
                            .withValue(value)
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
            String body = response.getBodyText(droppedBodyText);
            if (this.masking != null) {
                body = this.masking.maskResponseBody(body, contentType);
            }

            harContentBuilder.withText(body);
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

    public void build() throws IOException {
        DefaultHarStreamWriter harWriter = harWriterBuilder
                .withOutputStream(outputStream)
                .withComment(String.format("request capture for %s", resolveURL(this.request)))
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

    private String resolveURL(SpeakeasyRequest request) {
        String uri = request.getRequestURI();

        try {
            URL url = new URL(uri);

            String scheme = url.getProtocol();

            String proxyScheme = getScheme(request);
            if (StringUtils.hasText(proxyScheme)) {
                scheme = proxyScheme;
            }

            String host = url.getHost();

            if (request.getHeaders().containsKey("x-forwarded-host")) {
                List<String> headers = request.getHeaders().get("x-forwarded-host");
                if (headers != null && headers.size() > 0) {
                    host = headers.get(0);
                }
            } else if (request.getHeaders().containsKey("host")) {
                List<String> headers = request.getHeaders().get("host");
                if (headers != null && headers.size() > 0) {
                    host = headers.get(0);
                }
            }

            StringBuilder builder = new StringBuilder();

            builder
                    .append(scheme)
                    .append("://")
                    .append(host);

            if (!host.contains(":") && url.getPort() != -1 && url.getPort() != 80 && url.getPort() != 443) {
                builder.append(":").append(url.getPort());
            }

            builder.append(url.getPath());

            if (StringUtils.hasText(url.getQuery())) {
                builder.append("?").append(getMaskedQueryString(url.getQuery()));
            }

            if (StringUtils.hasText(url.getRef())) {
                builder.append("#").append(url.getRef());
            }

            return builder.toString();

        } catch (MalformedURLException e) {
            return uri;
        }
    }

    private String getScheme(SpeakeasyRequest request) {
        String scheme = "";

        if (request.getHeaders().containsKey("x-forwarded-proto")) {
            List<String> headers = request.getHeaders().get("x-forwarded-proto");

            if (headers != null && headers.size() > 0) {
                scheme = headers.get(0).toLowerCase();
            }
        } else if (request.getHeaders().containsKey("x-forwarded-scheme")) {
            List<String> headers = request.getHeaders().get("x-forwarded-scheme");

            if (headers != null && headers.size() > 0) {
                scheme = headers.get(0);
            }
        } else if (request.getHeaders().containsKey("forwarded")) {
            List<String> headers = request.getHeaders().get("forwarded");

            String forwarded = "";

            if (headers != null && headers.size() > 0) {
                forwarded = headers.get(0).toLowerCase();
            }

            if (forwarded != "") {
                Pattern pattern = Pattern.compile("(?i)(?:proto=)(https|http)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(forwarded);
                if (matcher.find()) {
                    scheme = matcher.group(1).toLowerCase();
                }
            }
        }

        return scheme;
    }

    private String getMaskedQueryString(String queryString) {
        if (queryString == null) {
            return "";
        }

        String[] params = queryString.split("&");
        List<String> maskedParams = new ArrayList<>();

        for (String param : params) {
            try {
                String[] keyValuePair = param.split("=");
                if (keyValuePair.length == 2) {
                    String key = URLDecoder.decode(keyValuePair[0], "UTF-8");
                    String value = URLDecoder.decode(keyValuePair[1], "UTF-8");

                    if (this.masking != null) {
                        String maskedValue = this.masking.getQueryStringMasks().get(key);
                        if (maskedValue != null) {
                            value = maskedValue;
                        }
                    }

                    maskedParams.add(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
                } else {
                    maskedParams.add(param);
                }
            } catch (UnsupportedEncodingException e) {
                maskedParams.add(param);
            }
        }

        return String.join("&", maskedParams);
    }
}
