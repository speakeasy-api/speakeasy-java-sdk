package dev.speakeasyapi.sdk;

public class SpeakeasyCookie {
    private final String name;
    private final String value;
    private final String domain;
    private final String path;
    private final String expires;
    private final boolean httpOnly;
    private final boolean secure;

    public SpeakeasyCookie(String name, String value) {
        this(name, value, null, null, null, false, false);
    }

    public SpeakeasyCookie(String name, String value, String domain, String path, String expires,
            boolean httpOnly, boolean secure) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expires = expires;
        this.httpOnly = httpOnly;
        this.secure = secure;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public String getExpires() {
        return expires;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public Boolean getSecure() {
        return secure;
    }
}
