package dev.speakeasyapi.micronaut.implementation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import dev.speakeasyapi.sdk.SpeakeasyCookie;
import dev.speakeasyapi.sdk.SpeakeasyRequest;
import dev.speakeasyapi.sdk.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

public class SpeakeasyNettyRequest implements SpeakeasyRequest {
    private final HttpRequest request;
    private SpeakeasyCaptureWriter writer;
    private final String requestId;

    public SpeakeasyNettyRequest(final HttpRequest request) {
        this.request = request;

        this.requestId = UUID.randomUUID().toString();

        SpeakeasySingleton.getInstance().registerRequest(this.requestId);

        this.request.headers().add("X-Speakeasy-Request-Id", this.requestId);
    }

    public void register(final SpeakeasyCaptureWriter writer) {
        this.writer = writer;
    }

    public String getRequestId() {
        return requestId;
    }

    public void removeRequestId() {
        this.request.headers().remove("X-Speakeasy-Request-Id");
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void buffer(final ByteBuf content) {
        // TODO may need to make this thread safe
        writer.writeRequest(content);
    }

    public Map<String, List<String>> getHeaders() {
        if (this.request.headers() != null) {
            return request.headers().entries().stream().collect(Collectors.groupingBy(Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }

        return null;
    }

    public List<SpeakeasyCookie> getCookies() {
        if (this.request.headers() != null) {
            final List<String> cookieHeaders = this.request.headers().getAll(HttpHeaderNames.COOKIE);

            if (cookieHeaders != null) {
                return cookieHeaders.stream().map(cookieHeader -> {
                    Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieHeader);

                    return cookies.stream().map(cookie -> {
                        return new SpeakeasyCookie(cookie.name(), cookie.value());
                    });
                }).flatMap(cookie -> cookie).collect(Collectors.toList());
            }
        }

        return null;
    }

    public String getContentType() {
        if (this.request.headers() != null) {
            return this.request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        }

        return null;
    }

    public String getBodyText(String droppedText) {
        if (this.writer.isReqValid()) {
            return new String(this.writer.getReqBuffer());
        }

        return droppedText;
    }

    public String getQueryString() {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(this.request.uri());

        return queryStringDecoder.rawQuery();
    }

    public Long getContentLength() {
        return HttpUtil.getContentLength(this.request, 0l);
    }

    public Long getHeaderSize() {
        return Utils.calculateHeaderSize(getHeaders());
    }

    public String getProtocol() {
        return this.request.protocolVersion().text();
    }

    public String getMethod() {
        return this.request.method().name();
    }

    public String getRequestURI() {
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(this.request.uri())
                .query("")
                .build();

        return uriComponents.toUriString();
    }
}
