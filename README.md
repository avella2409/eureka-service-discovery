# Eureka Service Discovery / Spring cloud load balancer

## Goal

Implement eureka service discovery and use spring cloud load balancer. 

We will have one eureka server (In production it is better to have multiple eureka server to have high availability), and two eureka service. The first service will call the second service using our eureka server registry and spring cloud load balancing. The second service will just have an endpoint returning a UUID that we will use to recognize which service we are calling when we load balance. We will launch two of the service returning a UUID on different port to observe the load balancing effect.

## Steps

### Step 1 : Create the eureka server

Add the `@EnableEurekaServer` annotation.

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```

We use only one eureka server, so we specify to not try to register with other server.
```java
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### Step 2 : Create the service that return a UUID

We add the `@EnableEurekaClient` annotation to make the application register to the eureka server registry and we create a simple endpoint that return a UUID. 

```java
@SpringBootApplication
@EnableEurekaClient
public class EurekaService2Application {

    private final String id = UUID.randomUUID().toString();

    @Bean
    RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
                .GET("/id", serverRequest -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(
                                Mono.just(id), String.class
                        ))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaService2Application.class, args);
    }
}
```

We specify the name of the application in the properties file, it will be used by other service to discover the location of this service using the eureka server registry.

```properties
spring.application.name=eureka-service-2
```

### Step 3 : Create the service that talk to the service returning a UUID

We add the `@EnableEurekaClient` annotation to make the application register to the eureka server registry but also later to retrieve all available instance of the service returning a UUID.

```java
@SpringBootApplication
@EnableEurekaClient
public class EurekaService1Application
```

To load balance request across available instance we use the annotation `@LoadBalanced` on the `WebClient.Builder` 

```java
    @Bean
    @LoadBalanced
    WebClient.Builder builder() {
        return WebClient.builder();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
```

By default, netflix ribbon load balancer will be used but if we want the spring cloud load balancer that use reactor we need to specify it in the properties with `spring.cloud.loadbalancer.ribbon.enabled=false`.

```properties
spring.application.name=eureka-service-1
server.port=8083
spring.cloud.loadbalancer.ribbon.enabled=false
```

We create a simple route to test that we communicate with the eureka server registry and load balance across available instance.

```java
    @Bean
    RouterFunction<ServerResponse> route(WebClient webClient) {
        return RouterFunctions.route()
                .GET("/data", serverRequest -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(
                                webClient.get().uri("lb://eureka-service-2/id")
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .map(id -> "Current id is " + id)
                                , String.class
                        )
                )
                .build();
    }

```

### Final Step : Launch everything and test

- Start the eureka server application
- Start two instance on different port of the service returning a UUID
- Start the service that talk with the service returning the UUID
- Go to the endpoint `/data`

Every time we refresh we should see a different ID being displayed, it means we retrieve correctly from the eureka server registry the location of all available service returning a UUID, and we load balance correctly across them.
