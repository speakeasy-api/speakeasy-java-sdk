package dev.speakeasyapi.springboot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

public class RequestResponseCaptureWatcher {
    private final long defaultMaxCaptureSize = 1 * 1024 * 1024;
    private long maxCaptureSize;

    public RequestResponseCaptureWatcher() {
        this.maxCaptureSize = defaultMaxCaptureSize;
    }

    public RequestResponseCaptureWatcher(long maxCaptureSize) {
        this.maxCaptureSize = maxCaptureSize;
    }

    private boolean requestIsValid = true;

    public boolean getRequestIsValid() {
        return requestCache.size() <= maxCaptureSize;
    }

    public void withServletRequest(HttpServletRequest request) {
        try {
            IOUtils.copy(request.getInputStream(), requestCache);
        } catch (IOException e) {
            // The input stream may be closed already if the request method
            // is GET.
            this.requestCache = new ByteArrayOutputStream();
        }
    }

    public ServletInputStream getCopiedInputStream() {
        ByteArrayInputStream wrappedInputStream = new ByteArrayInputStream(requestCache.toByteArray());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return wrappedInputStream.read();
            }
        };
    }

    private ByteArrayOutputStream requestCache = new ByteArrayOutputStream();

    public ByteArrayOutputStream getRequestCache() {
        return this.requestCache;
    }

    private boolean responseIsValid = true;

    public boolean getResponseIsValid() {
        return responseIsValid;
    }

    private long realResponseBodySize = 0;

    public long getRealResponseBodySize() {
        return realResponseBodySize;
    }

    private ServletOutputStream responseOutputStream;

    public RequestResponseCaptureWatcher withResponseOutputStream(ServletOutputStream outputStream) {
        this.responseOutputStream = outputStream;
        return this;
    }

    private ByteArrayOutputStream responseCache = new ByteArrayOutputStream();

    public ByteArrayOutputStream getResponseCache() {
        return this.responseCache;
    }

    private boolean canWriteResponseCache() {
        boolean canCache = cacheSizeExceedsMaxCaptureSize();
        if (!canCache && this.responseIsValid) {
            this.responseIsValid = false;
        }
        return canCache;
    }

    public void writeResponse(int b) throws IOException {
        responseOutputStream.write(b);
        realResponseBodySize++;
        if (canWriteResponseCache()) {
            responseCache.write(b);
        }
    }

    private boolean cacheSizeExceedsMaxCaptureSize() {
        return (this.requestIsValid && this.responseIsValid
                && 1 + requestCache.size() + responseCache.size() <= maxCaptureSize);
    }
}
