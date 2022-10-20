package dev.speakeasyapi.sdk;

import java.util.List;
import java.util.Map;

public interface SpeakeasyRequest {
    Map<String, List<String>> getHeaders();

    List<SpeakeasyCookie> getCookies();

    String getContentType();

    String getBodyText(String droppedText);

    String getQueryString();

    Long getContentLength();

    Long getHeaderSize();

    String getProtocol();

    String getMethod();

    String getRequestURI();
}
