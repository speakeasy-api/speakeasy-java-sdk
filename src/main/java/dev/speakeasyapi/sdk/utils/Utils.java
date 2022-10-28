package dev.speakeasyapi.sdk.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.speakeasyapi.sdk.SpeakeasyCookie;

public class Utils {
    public static BinaryOperator<List<String>> merge = new BinaryOperator<List<String>>() {
        @Override
        public List<String> apply(List<String> o, List<String> o2) {
            o.addAll(o2);
            return o;
        }
    };

    public static long calculateHeaderSize(Map<String, List<String>> headers) {
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

    public static SpeakeasyCookie parseSetCookieString(String cookie, Instant startTime) {
        String cookieName = null;
        String cookieValue = null;
        String domain = null;
        String path = null;
        String expires = null;
        boolean httpOnly = false;
        boolean secure = false;

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
                        domain = value;
                        break;
                    case "max-age": {
                        int maxAge = Integer.parseInt(value);

                        Instant expiresTime = startTime.plus(maxAge, ChronoUnit.SECONDS);

                        expires = expiresTime.toString();
                        break;
                    }
                    case "expires": {
                        try {
                            SimpleDateFormat inFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
                            Date expiresDate = inFormat.parse(value);

                            SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                            outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            expires = outFormat.format(expiresDate);
                        } catch (Exception e) {
                        }
                        break;
                    }
                    case "httponly":
                        httpOnly = true;
                        break;
                    case "path":
                        path = value;
                        break;
                    case "secure":
                        secure = true;
                        break;
                    default:
                        cookieName = key;
                        cookieValue = value;
                        break;
                }
            }
        }
        return new SpeakeasyCookie(cookieName, cookieValue, domain, path, expires, httpOnly, secure);
    }
}
