package dev.speakeasyapi.springboot;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import dev.speakeasyapi.sdk.SpeakeasyCookie;
import dev.speakeasyapi.sdk.SpeakeasyResponse;
import dev.speakeasyapi.sdk.utils.Utils;
import io.netty.handler.codec.http.HttpHeaderNames;

public class SpeakeasyServletResponse implements SpeakeasyResponse {
    private final HttpServletResponse response;
    private final RequestResponseCaptureWatcher watcher;

    public SpeakeasyServletResponse(HttpServletResponse response, RequestResponseCaptureWatcher watcher) {
        this.response = response;
        this.watcher = watcher;
    }

    public Map<String, List<String>> getHeaders() {
        return this.response.getHeaderNames().stream()
                .collect(Collectors.toMap(h -> h.toLowerCase(), h -> new ArrayList<>(this.response.getHeaders(h)),
                        Utils.merge));
    }

    public List<SpeakeasyCookie> getCookies(Instant startTime) {
        Map<String, List<String>> headers = getHeaders();

        if (headers != null) {
            final List<String> cookieHeaders = headers.get(HttpHeaderNames.SET_COOKIE.toString());

            if (cookieHeaders != null) {
                return cookieHeaders.stream().map(cookieHeader -> {
                    return Utils.parseSetCookieString(cookieHeader, startTime);
                }).collect(Collectors.toList());
            }
        }

        return new ArrayList<SpeakeasyCookie>();
    }

    public String getContentType() {
        return this.response.getContentType();
    }

    public String getBodyText(String droppedText) {
        if (this.watcher.getResponseIsValid()) {
            ByteArrayOutputStream responseCache = this.watcher.getResponseCache();

            try {
                responseCache.flush();
            } catch (Exception e) {
            }

            return responseCache.toString();
        }

        return droppedText;
    }

    public Long getContentLength(boolean originalSize) {
        if (this.watcher.getResponseIsValid() || originalSize) {
            long size = this.watcher.getRealResponseBodySize();

            if (size > 0) {
                return size;
            }
        }

        return -1l;
    }

    public int getStatus() {
        return this.response.getStatus();
    }

    public String getLocationHeader() {
        return this.response.getHeader("Location");
    }

    public Long getHeaderSize() {
        return Utils.calculateHeaderSize(getHeaders());
    }
}
