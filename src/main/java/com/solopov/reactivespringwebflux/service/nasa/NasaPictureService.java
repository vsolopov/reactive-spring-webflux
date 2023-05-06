package com.solopov.reactivespringwebflux.service.nasa;

import com.fasterxml.jackson.databind.JsonNode;
import com.solopov.reactivespringwebflux.dto.Picture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class NasaPictureService {

    @Value("${nasa.api.url}")
    private String nasaBaseApiUrl;

    @Value("${nasa.api.key}")
    private String nasaApiKey;

    public Mono<String> getLargestPicture(int sol) {
       return WebClient.create(nasaBaseApiUrl).
                get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("apiKey", nasaApiKey)
                        .queryParam("sol", sol)
                        .build())
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(JsonNode.class))
//               .map(jsonNode -> jsonNode.get("photos"))                                 //previous reactive approach
//               .flatMapMany(Flux::fromIterable)                                         //previous reactive approach
//               .map(jsonNode -> jsonNode.get("image_src"))                              //previous reactive approach
//               .map(JsonNode::asText)                                                   //previous reactive approach
//                .flatMapMany(jsonNode-> Flux.fromIterable(fetchPictureUrls(jsonNode)))  //1st approach
                .map(this::fetchPictureUrls)                                              //2nd approach
                .flatMapMany(Flux::fromIterable)                                          //2nd approach
                .flatMap(pictureUrl ->
                        WebClient.create(pictureUrl)
                                .head()
                                .exchangeToMono(ClientResponse::toBodilessEntity)
                                .map(HttpEntity::getHeaders)
                                .map(HttpHeaders::getLocation)
                                .map(URI::toString)
                                .flatMap(redirectedUrl -> WebClient.create(redirectedUrl)
                                        .head().exchangeToMono(ClientResponse::toBodilessEntity)
                                        .map(HttpEntity::getHeaders)
                                        .map(HttpHeaders::getContentLength)
                                        .map(length -> new Picture(redirectedUrl, length))
                                )
                ).reduce((pic1, pic2)-> pic1.size() > pic1.size() ? pic1 : pic2)
                .map(Picture::url);
    }

    private List<String> fetchPictureUrls(JsonNode jsonNode) {
        return StreamSupport.stream(jsonNode.get("photos").spliterator(), false)
                .map(photoJson -> photoJson.get("image_src"))
                .map(JsonNode::asText).toList();
    }

}
