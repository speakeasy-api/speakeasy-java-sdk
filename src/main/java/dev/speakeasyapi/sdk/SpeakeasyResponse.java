package dev.speakeasyapi.sdk;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface SpeakeasyResponse {
    Map<String, List<String>> getHeaders();

    List<SpeakeasyCookie> getCookies(Instant startTime);

    String getContentType();

    String getBodyText(String droppedText);

    Long getContentLength(boolean originalSize);

    int getStatus();

    String getLocationHeader();

    Long getHeaderSize();
}
