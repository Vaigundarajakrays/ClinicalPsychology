package com.clinicalpsychology.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AiTherapistService {

    private final WebClient webClient;
    private final WebClient moderationClient;

    public AiTherapistService(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        this.moderationClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/moderations")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    // For this no credits needed, it is free
    public Mono<Boolean> isFlaggedByModeration(String input){

        Map<String, Object> body = Map.of("input", input);

        return moderationClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.path("results").get(0).path("flagged").asBoolean(false))
                .onErrorReturn(false); // fallback to false if API call fails

    }

    public Flux<String> streamResponse(String userMessage) {
        Map<String, Object> request = Map.of(
                "model", "gpt-4o-mini",
                "stream", true,
                "max_tokens", 300,
                "temperature", 1.0,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                                    You are a helpful AI therapist. Keep your responses professional and concise. Never exceed 300 tokens in your replies. Use short, clear sentences. If asked about your identity, always respond: "I am PsyConnect's AI model — your personal support companion."
                                                   """
                        ),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    String rawChunk = new String(bytes, StandardCharsets.UTF_8);
                    return Flux.fromArray(rawChunk.split("\n"));
                })
                .filter(line -> line.startsWith("data: ") && !line.contains("[DONE]"))
                .flatMap(line -> {
                    try {
                        String json = line.substring("data: ".length());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(json);
                        JsonNode contentNode = root.path("choices").get(0).path("delta").path("content");
                        if (!contentNode.isMissingNode()) {
                            return Flux.just(" " +contentNode.asText()); // the space before done is very important, that is how frontend is expecting
                        }
                    } catch (Exception e) {
                        return Flux.just(" [ERROR] " + e.getMessage()); // the space before done is very important, that is how frontend is expecting
                    }
                    return Flux.empty();
                })
                .concatWith(Flux.just(" [DONE]")); // the space before done is very important, that is how frontend is expecting

    }

}
