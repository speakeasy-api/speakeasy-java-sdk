package dev.speakeasyapi.springboot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import dev.speakeasyapi.sdk.SpeakeasyCookie;
import dev.speakeasyapi.sdk.SpeakeasyRequest;
import dev.speakeasyapi.sdk.utils.Utils;

public class SpeakeasyServletRequest implements SpeakeasyRequest {
    private final HttpServletRequest request;
    private final RequestResponseCaptureWatcher watcher;

    public SpeakeasyServletRequest(HttpServletRequest request, RequestResponseCaptureWatcher watcher) {
        this.request = request;
        this.watcher = watcher;
    }

    public Map<String, List<String>> getHeaders() {
        if (request.getHeaderNames() != null) {
            return Collections.list(request.getHeaderNames()).stream()
                    .collect(Collectors.toMap(h -> h.toLowerCase(), h -> Collections.list(request.getHeaders(h)),
                            Utils.merge));
        }

        return null;
    }

    public List<SpeakeasyCookie> getCookies() {
        Cookie[] cookies = this.request.getCookies();

        if (cookies != null) {
            return Arrays.asList(cookies).stream()
                    .map(c -> new SpeakeasyCookie(c.getName(), c.getValue()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<SpeakeasyCookie>();
    }

    public String getContentType() {
        return request.getContentType();
    }

    public String getBodyText(String droppedText) {
        if (this.watcher.getRequestIsValid()) {
            return this.watcher.getRequestCache().toString();
        }

        return droppedText;
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public Long getContentLength() {
        return request.getContentLengthLong();
    }

    public Long getHeaderSize() {
        return Utils.calculateHeaderSize(getHeaders());
    }

    public String getProtocol() {
        return request.getProtocol();
    }

    public String getMethod() {
        return request.getMethod();
    }

    public String getRequestURI() {
        return request.getRequestURI();
    }
}
