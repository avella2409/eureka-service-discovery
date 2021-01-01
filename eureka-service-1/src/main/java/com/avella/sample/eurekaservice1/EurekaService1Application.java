package com.avella.sample.eurekaservice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
@EnableEurekaClient
public class EurekaService1Application {

    @Bean
    @LoadBalanced
    WebClient.Builder builder() {
        return WebClient.builder();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

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

    public static void main(String[] args) {
        SpringApplication.run(EurekaService1Application.class, args);
    }

}
