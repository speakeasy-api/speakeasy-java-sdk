package dev.speakeasyapi.springboot;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component("SpeakeasyFilter")
public class SpeakeasyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        RequestResponseCaptureWatcher watcher = new RequestResponseCaptureWatcher();
        chain.doFilter(
                new SpeakeasyRequestWrapper((HttpServletRequest) request, watcher),
                new SpeakeasyResponseWrapper((HttpServletResponse) response, watcher));
    }
}
