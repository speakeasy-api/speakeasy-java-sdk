package dev.speakeasyapi.springboot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("Succeeds with speakeasy har test fixtures")
public class SpeakeasyRequestResponseHandlerTest {

    private static Stream<Arguments> testProvider() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Stream.Builder<Arguments> tests = Stream.builder();

        URI uri = SpeakeasyRequestResponseHandlerTest.class.getResource("/testdata").toURI();
        Path dirPath = Paths.get(uri);
        Files.list(dirPath).forEach(p -> {
            assertDoesNotThrow(() -> {
                if (p.toString().endsWith("_input.json")) {
                    TestFixture test = mapper.readValue(p.toFile(), TestFixture.class);

                    Path output = p.getParent()
                            .resolve(p.getFileName().toString().replace("_input.json", "_output.json"));

                    test.WantHAR = new String(Files.readAllBytes(output));

                    tests.add(Arguments.of(test.Name, test));
                }
            });
        });

        return tests.build();
    }

    @DisplayName("Should generate expected har from request")
    @ParameterizedTest(name = "{index} => name={0}")
    @MethodSource("testProvider")
    public void testFixture(String name, TestFixture test) throws Exception {
        Instant startTime = null;

        if (test.Args.RequestStartTime != null) {
            startTime = Instant.parse(test.Args.RequestStartTime);
        } else {
            startTime = Instant.parse("2020-01-01T00:00:00.000Z");
            assertEquals("2020-01-01T00:00:00Z", startTime.toString());
        }

        MockHttpServletRequest request = new MockHttpServletRequest(test.Args.Method, test.Args.URL);

        String[] queryStringParts = test.Args.URL.split("\\?");
        if (queryStringParts.length > 1) {
            request.setQueryString(queryStringParts[1]);
        }

        List<Cookie> cookies = new ArrayList<Cookie>();

        if (test.Args.Headers != null) {
            for (Header header : test.Args.Headers) {
                for (String value : header.Values) {
                    if (header.Key.equalsIgnoreCase("Cookie")) {
                        String[] cookieStrings = value.split("; ");
                        for (String cookieString : cookieStrings) {
                            String[] cookieParts = cookieString.split("=");
                            Cookie cookie = new Cookie(cookieParts[0], cookieParts[1]);
                            cookies.add(cookie);
                        }
                    } else {
                        request.addHeader(header.Key, value);
                    }
                }
            }
        }

        request.setCookies(cookies.toArray(new Cookie[] {}));

        if (test.Args.Body != null && test.Args.Body.length() > 0) {
            request.setContent(test.Args.Body.getBytes());
        }

        RequestResponseCaptureWatcher watcher = new RequestResponseCaptureWatcher(test.Fields.MaxCaptureSize);
        HttpServletRequest wrappedRequest = new SpeakeasyRequestWrapper(request, watcher);
        MockHttpServletResponse response = new MockHttpServletResponse(startTime);
        HttpServletResponse wrappedResponse = new SpeakeasyResponseWrapper(response, watcher);

        if (test.Args.ResponseStatus > 0) {
            wrappedResponse.setStatus(test.Args.ResponseStatus);
        }

        if (test.Args.ResponseHeaders != null) {
            for (Header header : test.Args.ResponseHeaders) {
                for (String value : header.Values) {
                    wrappedResponse.addHeader(header.Key, value);
                }
            }
        }

        if (test.Args.ResponseBody != null && test.Args.ResponseBody.length() > 0) {
            wrappedResponse.getOutputStream().write(test.Args.ResponseBody.getBytes());
            wrappedResponse.getOutputStream().flush();
        }

        Instant endTime = startTime.plus(test.Args.ElapsedTime != 0 ? test.Args.ElapsedTime : 1, ChronoUnit.MILLIS);

        TestSpeakeasyClient client = new TestSpeakeasyClient();
        SpeakeasyRequestResponseHandler handler = new SpeakeasyRequestResponseHandler(client,
                LoggerFactory.getLogger(SpeakeasyRequestResponseHandlerTest.class),
                wrappedRequest, wrappedResponse, watcher, startTime, endTime, null, null);
        handler.run();

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> wantHar = (Map<String, Object>) (om.readValue(test.WantHAR, Map.class));
        Map<String, Object> gotHar = (Map<String, Object>) (om.readValue(client.HarString, Map.class));

        assertEquals(wantHar, gotHar);
    }
}

class Header {
    @JsonProperty("key")
    String Key;
    @JsonProperty("values")
    String[] Values;
}

class Fields {
    @JsonProperty("max_capture_size")
    public int MaxCaptureSize;
}

class Args {
    @JsonProperty("method")
    public String Method;
    @JsonProperty("url")
    public String URL;
    @JsonProperty("headers")
    public Header[] Headers;
    @JsonProperty("body")
    public String Body;
    @JsonProperty("request_start_time")
    public String RequestStartTime;
    @JsonProperty("elapsed_time")
    public int ElapsedTime;
    @JsonProperty("response_status")
    public int ResponseStatus;
    @JsonProperty("response_body")
    public String ResponseBody;
    @JsonProperty("response_headers")
    public Header[] ResponseHeaders;
}

class TestFixture {
    @JsonProperty("name")
    public String Name;
    @JsonProperty("fields")
    public Fields Fields;
    @JsonProperty("args")
    public Args Args;
    public String WantHAR;
}
