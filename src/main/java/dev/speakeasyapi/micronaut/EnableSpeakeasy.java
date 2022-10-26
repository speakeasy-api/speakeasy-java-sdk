package dev.speakeasyapi.micronaut;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.micronaut.aop.Around;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Around
public @interface EnableSpeakeasy {
}
