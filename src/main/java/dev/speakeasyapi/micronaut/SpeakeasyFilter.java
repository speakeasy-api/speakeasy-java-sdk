package dev.speakeasyapi.micronaut;

import org.reactivestreams.Publisher;

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
                    return true;
                }).subscribeOn(Schedulers.boundedElastic()).flux())
                .switchMap(aBoolean -> chain.proceed(request))
                .doOnNext(res -> {
                });
    }
}
