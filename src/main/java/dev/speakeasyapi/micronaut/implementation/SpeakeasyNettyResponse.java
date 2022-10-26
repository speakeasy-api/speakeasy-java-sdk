package dev.speakeasyapi.micronaut.implementation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.speakeasyapi.sdk.SpeakeasyCookie;
import dev.speakeasyapi.sdk.SpeakeasyResponse;
import dev.speakeasyapi.sdk.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;

public class SpeakeasyNettyResponse implements SpeakeasyResponse {
    private final HttpResponse response;
    private SpeakeasyCaptureWriter writer;

    public SpeakeasyNettyResponse(final HttpResponse response) {
        this.response = response;
    }

    public void register(final SpeakeasyCaptureWriter writer) {
        this.writer = writer;
    }

    public void buffer(final ByteBuf content) {
        // TODO may need to make this thread safe
        writer.writeResponse(content);
    }

    public Map<String, List<String>> getHeaders() {
        if (this.response.headers() != null) {
            return response.headers().entries().stream().collect(Collectors.groupingBy(Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }

        return null;
    }

    public List<SpeakeasyCookie> getCookies(Instant startTime) {
        if (this.response.headers() != null) {
            final List<String> cookieHeaders = this.response.headers().getAll(HttpHeaderNames.SET_COOKIE);

            if (cookieHeaders != null) {
                return cookieHeaders.stream().map(cookieHeader -> {
                    return Utils.parseSetCookieString(cookieHeader, startTime);
                }).collect(Collectors.toList());
            }
        }

        return null;
    }

    public String getContentType() {
        if (this.response.headers() != null) {
            return this.response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        }

        return null;
    }

    public String getBodyText(String droppedText) {
        if (this.writer.isResValid()) {
            return new String(this.writer.getResBuffer());
        }

        return droppedText;
    }

    public Long getContentLength(boolean originalSize) {
        if (writer.isResValid() || originalSize) {
            long size = HttpUtil.getContentLength(response, -1l);

            if (size > 0) {
                return size;
            }
        }

        return -1l;
    }

    public int getStatus() {
        return response.status().code();
    }

    public String getLocationHeader() {
        if (this.response.headers() != null) {
            return this.response.headers().get(HttpHeaderNames.LOCATION);
        }

        return null;
    }

    public Long getHeaderSize() {
        return Utils.calculateHeaderSize(getHeaders());
    }
}
