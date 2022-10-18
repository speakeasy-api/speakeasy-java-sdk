# Speakeasy JVM SDK

![speakeasy-logo](https://user-images.githubusercontent.com/68016351/180100416-b66263e6-1607-4465-b45d-0e298a67c397.png)

Speakeasy is your API Platform team as a service. Use our drop in SDK to manage all your API Operations including
customer facing usage embeds, test case generation from traffic, and understanding API drift.

This is the Speakeasy JVM SDK for evaluating API requests/responses. Currently only supports the Springboot API
Framework. If you are integrating for the first time please start at
the [Quickstart](https://docs.speakeasyapi.dev/overview/quickstart) on our docs home.

## Requirements

- Spring Boot 2.7.X
- Java 8+ (Also works with Kotlin, official support coming soon)

## Usage

### Minimum configuration

Add the Speakeasy SDK to your project's build.gradle file:

```groovy
dependencies {
    implementation 'dev.speakeasyapi:speakeasyapi-jvm-springboot-sdk:1.1.2'
}
```

Please check on [maven central](https://mavenlibs.com/maven/pom/dev.speakeasyapi/speakeasyapi-jvm-springboot-sdk) for
the latest release of the SDK.
Add to your SpringBoot application:

[Sign up for free on our platform](https://www.speakeasyapi.dev/). After you've created a workspace and generated an API
key enable Speakeasy in your SpringBoot API as follows:

```java

@EnableConfigurationProperties(EnableSpeakeasy.class)
public class SpringWebApp {
    // ...
}
```

Add your api key to `application.properties`:

```
speakeasy-api.apiKey=[your-api-key]
speakeasy-api.apiID=[your-api-id]
speakeasy-api.versionID=[your-api-version-id]
```

Build and deploy your app and that's it. Your API is being tracked in the Speakeasy workspace you just created
and will be visible on the dashboard next time you log in. Visit our [docs site](https://docs.speakeasyapi.dev/) to
learn more.

### Advanced configuration

The Speakeasy SDK provides both a global and per Api configuration option. If you want to use the SDK to track multiple
Apis or Versions from the same service you can configure individual instances of the SDK, by creating your
own `WebMvcConfigurer` implementation for each instance of the SDK, like the example below:

```java
package dev.speakeasyapi.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.speakeasyapi.sdk.utils.SpeakeasyFilter;
import dev.speakeasyapi.sdk.utils.SpeakeasyInterceptor;

@Configuration
@ConfigurationProperties(prefix = "myspeakeasyinstancev1")
@ConfigurationPropertiesScan
@Import(SpeakeasyFilter.class) // This enables request and response capture and is a requirement for the SDK to work
public class MySpeakeasyInstanceV1 implements WebMvcConfigurer {
    private String apiKey;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpeakeasyInterceptor(apiKey, "users", "v1.0.0"))
                .addPathPatterns("/v1/users/**") // Match paths
                .excludePathPatterns("/v1/products/**"); // Exclude paths
    }
}

```

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
@GetMapping("/v1/users/{id}") // The path template "/v1/users/{id}" is captured automatically by the SDK
public String getUser(@PathVariable("id") String id){
        // your handler logic here
        }
```

This isn't always successful or desired, meaning requests received by Speakeasy will be marked as `unmatched`, and
potentially not associated with your Api, Version or ApiEndpoints in the Speakeasy Dashboard.

To help the SDK in these situations you can provide path hints per request handler that match the paths in your OpenAPI
Schema:

```java
import dev.speakeasyapi.sdk.utils.SpeakeasyInterceptor;

@GetMapping("/")
public String handle(@RequestAttribute(SpeakeasyInterceptor.ControllerKey) SpeakeasyMiddlewareController controller){

        controller.setPathHint("/v1/users/{id}"); // This path hint will be used to match requests to your OpenAPI Schema

        // your handler logic here
        }
```

## Capturing Customer IDs

To help associate requests with customers/users of your APIs you can provide a customer ID per request handler:

```java
@GetMapping("/v1/users/{id}") // The path template "/v1/users/{id}" is captured automatically by the SDK
public String getUser(@PathVariable("id") String id,@RequestAttribute(SpeakeasyInterceptor.ControllerKey) SpeakeasyMiddlewareController controller){
        controller.setCustomerID("a-customers-id"); // This customer ID will be used to associate this instance of a request with your customers/users

        // your handler logic here
        }
```

Note: This is not required, but is highly recommended. By setting a customer ID you can easily associate requests with
your customers/users in the Speakeasy Dashboard, powering filters in the Request
Viewer [(Coming soon)](https://docs.speakeasyapi.dev/speakeasy-user-guide/request-viewer-coming-soon).

## Embedded Request Viewer Access Tokens

The Speakeasy SDK can generate access tokens for
the [Embedded Request Viewer](https://docs.speakeasyapi.dev/speakeasy-user-guide/request-viewer/embedded-request-viewer)
that allows your customers to self service API request logs.

For documentation on how to configure filters, find
that [HERE](https://docs.speakeasyapi.dev/speakeasy-user-guide/request-viewer/embedded-request-viewer).

Below are some examples on how to generate access tokens:

```java
@GetMapping("embed_access_token")
public String getSpeakeasyEmbedAccessToken(@RequestAttribute(SpeakeasyInterceptor.ControllerKey) SpeakeasyMiddlewareController controller){
        String customerId=null;

        // populate your customerId

        // Restrict data by customer id
        SpeakeasyEmbedAccessTokenRequestBuilder requestBuilder = new SpeakeasyEmbedAccessTokenRequestBuilder();
        requestBuilder.withCustomerFilter(customerId);

        // Restrict data by time (last 24 hours)
        Instant startTime=Instant.now().minusSeconds(60*60*24);
        requestBuilder.withTimeFilter(startTime,SpeakeasyEmbedAccessTokenRequestFilterOperator.GreaterThan);

        String embedAccessToken=controller.getEmbedAccessToken(requestBuilder.build());

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

## Running the Java Example Service (Coming Soon!)
