package dev.speakeasyapi.springboot;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.util.concurrent.MoreExecutors;

import dev.speakeasyapi.sdk.SpeakeasyConfig;
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;
import dev.speakeasyapi.sdk.client.ISpeakeasyClient;
import dev.speakeasyapi.sdk.client.SpeakeasyClient;

public class SpeakeasyInterceptor implements HandlerInterceptor {
    public static final String StartTimeKey = "speakeasyStartTime";

    private Executor pool;
    private final ISpeakeasyClient client;
    private Logger logger = LoggerFactory.getLogger(SpeakeasyInterceptor.class);

    public SpeakeasyInterceptor(SpeakeasyConfig cfg) {
        this(cfg, null);
    }

    public SpeakeasyInterceptor(SpeakeasyConfig cfg, ISpeakeasyClient client) {
        pool = Executors.newCachedThreadPool();

        if (!cfg.isIngestEnabled()) {
            pool = MoreExecutors.directExecutor();
        }

        this.client = client != null ? client
                : new SpeakeasyClient(cfg);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(SpeakeasyMiddlewareController.Key,
                new SpeakeasyMiddlewareController(this.client));
        request.setAttribute(StartTimeKey, Instant.now());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        SpeakeasyMiddlewareController controller = (SpeakeasyMiddlewareController) req
                .getAttribute(SpeakeasyMiddlewareController.Key);

        String pathHint;
        if (StringUtils.hasText(controller.getPathHint())) {
            pathHint = controller.getPathHint();
        } else {
            pathHint = getPathHint((HandlerMethod) handler);
        }
        Instant startTime = (Instant) req.getAttribute(StartTimeKey);
        RequestResponseCaptureWatcher watcher = (RequestResponseCaptureWatcher) req
                .getAttribute(SpeakeasyRequestWrapper.speakeasyRequestResponseWatcherAttribute);

        pool.execute(new SpeakeasyRequestResponseHandler(this.client, this.logger,
                req, res, watcher, startTime, Instant.now(), pathHint, controller.getCustomerID()));
    }

    private static String getPathHint(HandlerMethod hm) {
        String controllerPath = "";
        String pathHint = "";

        if (hm.getBean().getClass().isAnnotationPresent(RequestMapping.class)) {
            RequestMapping ctrlMapping = hm.getBean().getClass().getAnnotation(RequestMapping.class);
            if (ctrlMapping != null && ctrlMapping.value() != null && ctrlMapping.value().length > 0) {
                controllerPath = ctrlMapping.value()[0];

                if (pathHint.endsWith("/")) {
                    controllerPath = pathHint.substring(0, pathHint.length() - 1);
                }

                if (!controllerPath.startsWith("/")) {
                    controllerPath = "/" + controllerPath;
                }
            }
        }

        String methodPath = "";

        if (hm.hasMethodAnnotation(RequestMapping.class)) {
            RequestMapping mapping = hm.getMethodAnnotation(RequestMapping.class);
            if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (hm.hasMethodAnnotation(GetMapping.class)) {
            GetMapping mapping = hm.getMethodAnnotation(GetMapping.class);
            if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (hm.hasMethodAnnotation(PostMapping.class)) {
            PostMapping mapping = hm.getMethodAnnotation(PostMapping.class);
            if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (hm.hasMethodAnnotation(PutMapping.class)) {
            PutMapping mapping = hm.getMethodAnnotation(PutMapping.class);
            if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (hm.hasMethodAnnotation(DeleteMapping.class)) {
            DeleteMapping mapping = hm.getMethodAnnotation(DeleteMapping.class);
            if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (hm.hasMethodAnnotation(PatchMapping.class)) {
            PatchMapping mapping = hm.getMethodAnnotation(PatchMapping.class);
            if (mapping != null && mapping.value() != null && mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        }

        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }

        return controllerPath + methodPath;
    }
}
