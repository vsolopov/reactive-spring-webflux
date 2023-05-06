package com.solopov.reactivespringwebflux.controller;

import com.solopov.reactivespringwebflux.service.nasa.NasaPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/nasa/pictures")
@RequiredArgsConstructor
public class NasaPictureController {

    private final NasaPictureService nasaPictureService;

    @GetMapping(value = "/{sol}/largest", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getLargestPicture(@PathVariable int sol){
        return nasaPictureService.getLargestPicture(sol)
                .flatMap(url -> WebClient.create()
                        .mutate().codecs(config -> config.defaultCodecs().maxInMemorySize(10_000_000))
                        .build()
                        .get()
                        .exchangeToMono(resp-> resp.bodyToMono(byte[].class))
                );
    }

}
