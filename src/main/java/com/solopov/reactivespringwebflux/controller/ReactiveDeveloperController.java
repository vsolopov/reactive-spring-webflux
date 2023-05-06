package com.solopov.reactivespringwebflux.controller;

import com.solopov.reactivespringwebflux.dto.ReactiveDeveloperStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/reactive/persons")
@RequiredArgsConstructor
public class ReactiveDeveloperController {

    private final ResourceLoader resourceLoader;

    @GetMapping("/max")
    public Mono<ReactiveDeveloperStatus> getTheMostReactivePerson() {
        //todo:
        // 1. call the endpoint
        // 2. get the list of persons from the json
        // 3. find the person with nax reactive programming level
        // 4. return a person to the client


        // 4. return a person to the client
        return
                // 1. call the endpoint
                WebClient.create("http://127.0.0.1:8080/reactive/persons")
                        // 2. get the list of persons from the json
                        .get()
                        .exchangeToFlux(resp -> resp.bodyToFlux(ReactiveDeveloperStatus.class))
                        // 3. find the person with nax reactive programming level
                        .reduce((person1, person2) -> person1.reactiveProgrammingLevel() > person2.reactiveProgrammingLevel() ?
                                person1 : person2);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<String> getAllPersons(ServerWebExchange exchange) {
        Resource resource = resourceLoader.getResource("classpath:/response/reactive-developers.json");

        if (resource.exists()) {
            Flux<DataBuffer> flux = DataBufferUtils.read(resource, exchange.getResponse().bufferFactory(), StreamUtils.BUFFER_SIZE);
            return DataBufferUtils.join(flux)
                    .publishOn(Schedulers.boundedElastic())
                    .handle((dataBuffer, sink) -> {
                        try {
                            sink.next(new String(dataBuffer.asInputStream().readAllBytes()));
                        } catch (IOException e) {
                            sink.error(new RuntimeException("Error occurred while reading the file.", e));
                        }
                    });
        } else {
            return Mono.error(new RuntimeException("File not found."));
        }
    }
}
