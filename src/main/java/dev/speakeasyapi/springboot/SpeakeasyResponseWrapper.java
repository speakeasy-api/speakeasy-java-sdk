package dev.speakeasyapi.springboot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class SpeakeasyResponseWrapper implements HttpServletResponse {

    public class OutputStreamWrapper extends ServletOutputStream {

        private ServletOutputStream wrapped;
        RequestResponseCaptureWatcher watcher;

        public OutputStreamWrapper(ServletOutputStream wrapped, RequestResponseCaptureWatcher watcher) {

            watcher.withResponseOutputStream(wrapped);

            this.wrapped = wrapped;
            this.watcher = watcher;
        }

        @Override
        public boolean isReady() {
            return this.wrapped.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            this.wrapped.setWriteListener(listener);
        }

        @Override
        public void write(int b) throws IOException {
            this.watcher.writeResponse(b);
        }
    }

    private HttpServletResponse wrapped;
    private OutputStreamWrapper outputStreamWrapper;

    public SpeakeasyResponseWrapper(HttpServletResponse wrapped, RequestResponseCaptureWatcher watcher)
            throws IOException {
        this.outputStreamWrapper = new OutputStreamWrapper(wrapped.getOutputStream(), watcher);

        this.wrapped = wrapped;
    }

    public long getRealBodySize() {
        return this.outputStreamWrapper.watcher.getRealResponseBodySize();
    }

    @Override
    public void addCookie(Cookie cookie) {
        this.wrapped.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.wrapped.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return this.wrapped.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return this.wrapped.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return this.wrapped.encodeURL(url);
    }

    @Deprecated
    @Override
    public String encodeRedirectUrl(String url) {
        return this.wrapped.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.wrapped.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.wrapped.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.wrapped.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.wrapped.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.wrapped.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.wrapped.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.wrapped.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.wrapped.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.wrapped.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        this.wrapped.setStatus(sc);
    }

    @Deprecated
    @Override
    public void setStatus(int sc, String sm) {
        this.wrapped.setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return this.wrapped.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return this.wrapped.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.wrapped.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.wrapped.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return this.wrapped.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return this.wrapped.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.outputStreamWrapper;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.wrapped.getWriter();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.wrapped.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        this.wrapped.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long length) {
        this.wrapped.setContentLengthLong(length);
    }

    @Override
    public void setContentType(String type) {
        this.wrapped.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        this.wrapped.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return this.wrapped.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.wrapped.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        this.wrapped.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return this.wrapped.isCommitted();
    }

    @Override
    public void reset() {
        this.wrapped.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        this.wrapped.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return this.wrapped.getLocale();
    }
}
