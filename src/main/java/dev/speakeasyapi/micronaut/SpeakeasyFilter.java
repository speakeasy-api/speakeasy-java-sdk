package dev.speakeasyapi.micronaut;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;

import dev.speakeasyapi.micronaut.implementation.SpeakeasyRequestContext;
import dev.speakeasyapi.micronaut.implementation.SpeakeasySingleton;
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Filter("/**")
public class SpeakeasyFilter implements HttpServerFilter {
    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Flux
                .from(Mono.fromCallable(() -> {
                    String requestId = request.getHeaders().get("X-Speakeasy-Request-Id");

                    if (StringUtils.isNotEmpty(requestId)) {
                        SpeakeasyRequestContext context = SpeakeasySingleton.getInstance()
                                .getRequestContext(requestId);

                        if (context != null) {
                            SpeakeasyMiddlewareController ctrl = context.getController();
                            request.setAttribute(SpeakeasyMiddlewareController.Key, ctrl);
                        }
                    }

                    return true;
                }).subscribeOn(Schedulers.boundedElastic()).flux())
                .switchMap(aBoolean -> chain.proceed(request))
                .doOnNext(res -> {
                });
    }
}
