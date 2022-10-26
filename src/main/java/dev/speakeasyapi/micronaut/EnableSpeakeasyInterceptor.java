package dev.speakeasyapi.micronaut;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import dev.speakeasyapi.micronaut.implementation.SpeakeasySingleton;
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
    public static void Init(String apiKey, String apiID, String versionID) {
        SpeakeasySingleton.getInstance().configure(apiKey, apiID, versionID);
    }

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

        Method method = context.getTargetMethod();

        String methodPath = "";

        if (method.isAnnotationPresent(CustomHttpMethod.class)) {
            CustomHttpMethod anno = method.getAnnotation(CustomHttpMethod.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Get.class)) {
            Get anno = method.getAnnotation(Get.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Post.class)) {
            Post anno = method.getAnnotation(Post.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Put.class)) {
            Put anno = method.getAnnotation(Put.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Delete.class)) {
            Delete anno = method.getAnnotation(Delete.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Head.class)) {
            Head anno = method.getAnnotation(Head.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Options.class)) {
            Options anno = method.getAnnotation(Options.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Patch.class)) {
            Patch anno = method.getAnnotation(Patch.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        } else if (method.isAnnotationPresent(Trace.class)) {
            Trace anno = method.getAnnotation(Trace.class);

            if (anno.value() != "") {
                methodPath = anno.value();
            }
        }

        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }

        return controllerPath + methodPath;
    }
}
