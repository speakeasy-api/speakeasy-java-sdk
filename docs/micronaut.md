# Micronaut

## Requirements

- Micronaut 3.7.X
- Java 8+

## Usage

### Minimum configuration

Add the Speakeasy SDK to your project's build.gradle file:

```groovy
dependencies {
    implementation 'dev.speakeasyapi:speakeasyapi-java-sdk:1.3.0'
}
```

Please check on [maven central](https://mavenlibs.com/maven/pom/dev.speakeasyapi/speakeasyapi-java-sdk) for
the latest release of the SDK.

Add to your Micronaut application:

[Sign up for free on our platform](https://www.speakeasyapi.dev/). After you've created a workspace and generated an API
key enable Speakeasy in your Micronaut Application as follows:

```java
import dev.speakeasyapi.micronaut.EnableSpeakeasyInterceptor;
import dev.speakeasyapi.micronaut.SpeakeasyFilter;
import io.micronaut.context.annotation.Import;

// Import the Speakeasy SDK classes to inject into your application to allow request capture
@Import(classes = { SpeakeasyFilter.class, EnableSpeakeasyInterceptor.class }, annotated = "*")
public class Application {
    public static void main(String[] args) {
        // Setup the SDK on application startup
        EnableSpeakeasyInterceptor.configure(
                "YOUR API ID HERE", 	// enter a name that you'd like to associate captured requests with. 
        // This name will show up in the Speakeasy dashboard. e.g. "PetStore" might be a good ApiID for a Pet Store's API.
        // No spaces allowed.
                "YOUR VERSION ID HERE"	// enter a version that you would like to associate captured requests with.
        // The combination of ApiID (name) and VersionID will uniquely identify your requests in the Speakeasy Dashboard.
        // e.g. "v1.0.0". You can have multiple versions for the same ApiID (if running multiple versions of your API)
                "YOUR API KEY HERE",	// retrieve from Speakeasy API dashboard. This can be optionally left out of the configure call and loaded from the SPEAKEASY_API_KEY environment variable.
        );
        // ...
    }
}
```

Create a class called SpeakeasyNettyServerCustomizer.java in your project and add the following code:

```java
package dev.speakeasyapi.micronaut;

import dev.speakeasyapi.micronaut.implementation.SpeakeasyChannelDuplexHandler;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
import io.micronaut.http.server.netty.NettyServerCustomizer;
import io.netty.channel.Channel;
import jakarta.inject.Singleton;

@Singleton
public class SpeakeasyNettyServerCustomizer implements BeanCreatedEventListener<NettyServerCustomizer.Registry> {
    @Override
    public NettyServerCustomizer.Registry onCreated(BeanCreatedEvent<NettyServerCustomizer.Registry> event) {
        NettyServerCustomizer.Registry registry = event.getBean();
        registry.register(new Customizer(null));
        return registry;
    }

    private class Customizer implements NettyServerCustomizer {
        private final Channel channel;

        Customizer(Channel channel) {
            this.channel = channel;
        }

        @Override
        public NettyServerCustomizer specializeForChannel(Channel channel, ChannelRole role) {
            return new Customizer(channel);
        }

        @Override
        public void onStreamPipelineBuilt() {
            channel.pipeline().addBefore(ChannelPipelineCustomizer.HANDLER_HTTP_STREAM, "speakeasy",
                    new SpeakeasyChannelDuplexHandler());
        }
    }
}
```
The above class needs to live in the same package as your Application.java file. Due to limitations in the Micronaut framework, this class can't be imported from the SDK.

Add to the controllers and methods you want to capture:

```java
import dev.speakeasyapi.micronaut.EnableSpeakeasy;
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;

// Add the @EnableSpeakeasy annotation to the controller (or method) you want to capture to enable request capture
@EnableSpeakeasy
@Controller("/hello")
public class MyController {

    @Get(produces = MediaType.TEXT_PLAIN, value = "/world")
    public String myHandlerMethod(@RequestAttribute(SpeakeasyMiddlewareController.Key) SpeakeasyMiddlewareController ctrl) { // Add the SpeakeasyMiddlewareController to your controller method to allow working with the SDK but also to allow the Speakeasy SDK to understand that this method should be captured
        // ...
    }
}
```

Build and deploy your app and that's it. Your API is being tracked in the Speakeasy workspace you just created
and will be visible on the dashboard next time you log in. Visit our [docs site](https://docs.speakeasyapi.dev/) to
learn more.

### On-Premise Configuration

The SDK provides a way to redirect the requests it captures to an on-premise deployment of the Speakeasy Platform. This
is done through the use of environment variables listed below. These are to be set in the environment of your services
that have integrated the SDK:

* `SPEAKEASY_SERVER_URL` - The url of the on-premise Speakeasy Platform's GRPC Endpoint. By default this
  is `grpc.prod.speakeasyapi.dev:443`.
* `SPEAKEASY_SERVER_SECURE` - Whether or not to use TLS for the on-premise Speakeasy Platform. By default this is `true`
  set to `SPEAKEASY_SERVER_SECURE="false"` if you are using an insecure connection.

## Request Matching

The Speakeasy SDK out of the box will do its best to match requests to your provided OpenAPI Schema. It does this by
extracting the path template used by the Mapping annotation for your controller handlers and attempting to match it to
the paths defined in the OpenAPI Schema, for example:

```java
@Get("/v1/users/{id}") // The path template "/v1/users/{id}" is captured automatically by the SDK
public String getUser(@PathVariable("id") String id){
        // your handler logic here
}
```

This isn't always successful or desired, meaning requests received by Speakeasy will be marked as `unmatched`, and
potentially not associated with your Api, Version or ApiEndpoints in the Speakeasy Dashboard.

To help the SDK in these situations you can provide path hints per request handler that match the paths in your OpenAPI
Schema:

```java
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;

@Get("/")
public String handle(@RequestAttribute(SpeakeasyMiddlewareController.Key) SpeakeasyMiddlewareController ctrl){

        ctrl.setPathHint("/v1/users/{id}"); // This path hint will be used to match requests to your OpenAPI Schema

        // your handler logic here
}
```

## Capturing Customer IDs

To help associate requests with customers/users of your APIs you can provide a customer ID per request handler:

```java
@Get("/v1/users/{id}") // The path template "/v1/users/{id}" is captured automatically by the SDK
public String getUser(@PathVariable("id") String id,@RequestAttribute(SpeakeasyMiddlewareController.Key) SpeakeasyMiddlewareController ctrl){
        ctrl.setCustomerID("a-customers-id"); // This customer ID will be used to associate this instance of a request with your customers/users

        // your handler logic here
}
```

Note: This is not required, but is highly recommended. By setting a customer ID you can easily associate requests with
your customers/users in the Speakeasy Dashboard, powering filters in the [Request
Viewer](https://docs.speakeasyapi.dev/docs/using-speakeasy/understand-apis).

## Embedded Request Viewer Access Tokens

The Speakeasy SDK can generate access tokens for
the [API Request Viewer Component](https://docs.speakeasyapi.dev/docs/using-speakeasy/build-dev-portals/request-viewer)
that allows your customers to self service API request logs.

Below are some examples on how to generate access tokens:

```java
@Get("/embed_access_token")
ublic String getSpeakeasyEmbedAccessToken(@RequestAttribute(SpeakeasyMiddlewareController.Key) SpeakeasyMiddlewareController controller){
        String customerId=null;

        // populate your customerId

        // Restrict data by customer id
        SpeakeasyAccessTokenFilterBuilder filterBuilder = new SpeakeasyAccessTokenFilterBuilder();
        filterBuilder.withCustomerFilter(customerId);

        // Restrict data by time (last 24 hours)
        Instant startTime=Instant.now().minusSeconds(60*60*24);
        filterBuilder.withTimeFilter(startTime,SpeakeasyAccessTokenFilterOperator.GreaterThan);

        String embedAccessToken=controller.getEmbedAccessToken(filterBuilder.build());

        // build response
        }
```

## Setting up for Tests

To simplify your test experience, the `SPEAKEASY_TEST_MODE=true` environment variable makes 2
changes to how the SDK operates:

1. Request processing is moved to the main thread.
2. The grpc call to the Speakeasy API Platform is _not_ executed (so you don't need to change
   your `application.properties`).

## Building

```bash
gradle clean build
```