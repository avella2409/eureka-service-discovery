package com.avella.sample.eurekaservice2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
