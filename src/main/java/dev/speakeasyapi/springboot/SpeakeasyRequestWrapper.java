package dev.speakeasyapi.springboot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class SpeakeasyRequestWrapper implements HttpServletRequest {

    public static String speakeasyRequestResponseWatcherAttribute = "speakeasyRequestResponseWatcher";
    private HttpServletRequest wrapped;
    private RequestResponseCaptureWatcher watcher;

    public SpeakeasyRequestWrapper(HttpServletRequest wrapped, RequestResponseCaptureWatcher watcher) {
        watcher.withServletRequest(wrapped);

        this.watcher = watcher;
        this.wrapped = wrapped;
        wrapped.setAttribute(speakeasyRequestResponseWatcherAttribute, watcher);
    }

    @Override
    public String getAuthType() {
        return this.wrapped.getAuthType();
    }

    private Cookie[] cachedCookies;

    @Override
    public Cookie[] getCookies() {
        if (this.cachedCookies == null) {
            this.cachedCookies = this.wrapped.getCookies();
        }
        return this.cachedCookies;
    }

    @Override
    public long getDateHeader(String name) {
        return wrapped.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return wrapped.getHeader(name);
    }

    private Map<String, Collection<String>> cachedHeaders = new HashMap<>();

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (!this.cachedHeaders.containsKey(name)) {
            Enumeration<String> headers = this.wrapped.getHeaders(name);
            Collection<String> collection = new ArrayList<>();
            while (headers.hasMoreElements()) {
                collection.add(headers.nextElement());
            }
            this.cachedHeaders.put(name, collection);
        }
        return Collections.enumeration(this.cachedHeaders.get(name));
    }

    private Collection<String> cachedHeaderNames;

    @Override
    public Enumeration<String> getHeaderNames() {
        if (this.cachedHeaderNames == null) {
            Enumeration<String> headerNames = this.wrapped.getHeaderNames();
            Collection<String> collection = new ArrayList<>();
            while (headerNames.hasMoreElements()) {
                try {
                    collection.add(headerNames.nextElement());
                } catch (NullPointerException e) {
                    break;
                }
            }
            this.cachedHeaderNames = collection;
        }

        return Collections.enumeration(this.cachedHeaderNames);
    }

    @Override
    public int getIntHeader(String name) {
        return this.wrapped.getIntHeader(name);
    }

    private String cachedMethod;

    @Override
    public String getMethod() {
        if (this.cachedMethod == null) {
            this.cachedMethod = this.wrapped.getMethod();
        }
        return this.cachedMethod;
    }

    @Override
    public String getPathInfo() {
        return this.wrapped.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return this.wrapped.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return this.wrapped.getContextPath();
    }

    private String cachedQueryString;

    @Override
    public String getQueryString() {
        if (this.cachedQueryString == null) {
            this.cachedQueryString = this.wrapped.getQueryString();
        }
        return this.cachedQueryString;
    }

    @Override
    public String getRemoteUser() {
        return this.wrapped.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.wrapped.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.wrapped.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return this.wrapped.getRequestedSessionId();
    }

    private String cachedRequestURI;

    @Override
    public String getRequestURI() {
        if (this.cachedRequestURI == null) {
            this.cachedRequestURI = this.wrapped.getRequestURI();
        }
        return this.cachedRequestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return this.wrapped.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return this.wrapped.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return this.wrapped.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return this.wrapped.getSession();
    }

    @Override
    public String changeSessionId() {
        return this.wrapped.changeSessionId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.wrapped.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.wrapped.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.wrapped.isRequestedSessionIdFromURL();
    }

    @Deprecated
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return this.wrapped.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return this.wrapped.authenticate(response);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        this.wrapped.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        this.wrapped.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return this.wrapped.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return this.wrapped.getPart(name);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass)
            throws IOException, ServletException {
        return this.wrapped.upgrade(httpUpgradeHandlerClass);
    }

    @Override
    public Object getAttribute(String name) {
        return this.wrapped.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return this.wrapped.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return this.wrapped.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.wrapped.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        return this.wrapped.getContentLength();
    }

    private long cachedContentLengthLong = -1;

    @Override
    public long getContentLengthLong() {
        if (this.cachedContentLengthLong == -1) {
            this.cachedContentLengthLong = this.wrapped.getContentLengthLong();
        }
        return this.cachedContentLengthLong;
    }

    private String cachedContentType;

    @Override
    public String getContentType() {
        if (this.cachedContentType == null) {
            this.cachedContentType = this.wrapped.getContentType();
        }
        return this.cachedContentType;
    }

    @Override
    public ServletInputStream getInputStream() {
        return watcher.getCopiedInputStream();
    }

    @Override
    public String getParameter(String name) {
        return this.wrapped.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return this.wrapped.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.wrapped.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.wrapped.getParameterMap();
    }

    private String cachedProtocol;

    @Override
    public String getProtocol() {
        if (this.cachedProtocol == null) {
            this.cachedProtocol = this.wrapped.getProtocol();
        }
        return this.cachedProtocol;
    }

    @Override
    public String getScheme() {
        return this.wrapped.getScheme();
    }

    @Override
    public String getServerName() {
        return this.wrapped.getServerName();
    }

    @Override
    public int getServerPort() {
        return this.wrapped.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return this.wrapped.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return this.wrapped.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return this.wrapped.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.wrapped.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        this.wrapped.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        return this.wrapped.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return this.wrapped.getLocales();
    }

    @Override
    public boolean isSecure() {
        return this.wrapped.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.wrapped.getRequestDispatcher(path);
    }

    @Deprecated
    @Override
    public String getRealPath(String path) {
        return this.wrapped.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return this.wrapped.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return this.wrapped.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return this.wrapped.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return this.wrapped.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return this.wrapped.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return this.wrapped.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        return this.wrapped.startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return this.wrapped.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return this.wrapped.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.wrapped.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.wrapped.getDispatcherType();
    }
}
