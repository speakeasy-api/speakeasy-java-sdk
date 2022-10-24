package dev.speakeasyapi.micronaut;

import java.util.Map;
import java.util.Optional;

import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.CustomHttpMethod;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Head;
import io.micronaut.http.annotation.Options;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Trace;
import jakarta.inject.Singleton;

@Singleton
@InterceptorBean(EnableSpeakeasy.class)
public class EnableSpeakeasyInterceptor implements MethodInterceptor<Object, Object> {
    @Nullable
    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Map<String, Object> parameters = context.getParameterValueMap();

        SpeakeasyMiddlewareController ctrl = null;

        for (Object value : parameters.values()) {
            if (SpeakeasyMiddlewareController.class.isInstance(value)) {
                ctrl = (SpeakeasyMiddlewareController) value;
                break;
            }
        }

        if (ctrl != null) {
            ctrl.setEnabled(true);
            ctrl.setPathHint(getPathHint(context));
        }

        return context.proceed();
    }

    private static String getPathHint(MethodInvocationContext<Object, Object> context) {
        String controllerPath = "";
        String pathHint = "";

        AnnotationValue<Controller> controllerAnno = context.getAnnotation(Controller.class);
        if (controllerAnno != null) {
            Optional<String> controllerValue = controllerAnno.get("value", String.class);

            if (controllerValue.isPresent()) {
                controllerPath = controllerValue.get();

                if (pathHint.endsWith("/")) {
                    controllerPath = pathHint.substring(0, pathHint.length() - 1);
                }

                if (!controllerPath.startsWith("/")) {
                    controllerPath = "/" + controllerPath;
                }
            }
        }

        String methodPath = "";

        if (context.hasAnnotation(CustomHttpMethod.class)) {
            AnnotationValue<CustomHttpMethod> anno = context.getAnnotation(CustomHttpMethod.class);

            Optional<String> value = anno.get("value", String.class);
            if (value.isPresent()) {
                methodPath = value.get();
            }
        } else if (context.hasAnnotation(Get.class)) {
            AnnotationValue<Get> anno = context.getAnnotation(Get.class);

            Optional<String> value = anno.get("value", String.class);
            if (value.isPresent()) {
                methodPath = value.get();
            }
        } else if (context.hasAnnotation(Put.class)) {
            AnnotationValue<Put> anno = context.getAnnotation(Put.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        } else if (context.hasAnnotation(Post.class)) {
            AnnotationValue<Post> anno = context.getAnnotation(Post.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        } else if (context.hasAnnotation(Delete.class)) {
            AnnotationValue<Delete> anno = context.getAnnotation(Delete.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        } else if (context.hasAnnotation(Patch.class)) {
            AnnotationValue<Patch> anno = context.getAnnotation(Patch.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        } else if (context.hasAnnotation(Trace.class)) {
            AnnotationValue<Trace> anno = context.getAnnotation(Trace.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        } else if (context.hasAnnotation(Head.class)) {
            AnnotationValue<Head> anno = context.getAnnotation(Head.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        } else if (context.hasAnnotation(Options.class)) {
            AnnotationValue<Options> anno = context.getAnnotation(Options.class);

            Optional<String[]> value = anno.get("value", String[].class);
            if (value.isPresent()) {
                methodPath = value.get()[0];
            }
        }

        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }

        return controllerPath + methodPath;
    }
}
