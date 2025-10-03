package com.clinicalpsychology.app.aitherapist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                                        You are a helpful AI therapist. Keep your responses professional and concise. 
                                        Never exceed 300 tokens in your replies. Use short, clear sentences. 
                                
                                        Identity:
                                        - If asked about your identity, always reply:
                                          "I am PsyConnect's AI model — your personal support companion."
                                
                                        Capabilities:
                                        - If the user asks "What can you do?" or any similar question, 
                                          reply from the perspective of an AI therapist. 
                                          Explain that you can provide emotional support, help users reflect on feelings, 
                                          offer coping strategies, and suggest self-care practices. 
                                          Frame it as how you help them improve mental wellbeing and resilience, 
                                          not just a list of features.
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

                        // skip empty or heartbeat events
                        if (json.isBlank() || json.equals("[DONE]")) {
                            return Flux.empty();
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(json);
                        // Defensive check for choices array
                        JsonNode choices = root.path("choices");
                        if (!choices.isArray() || choices.size() == 0) {
                            log.warn("Skipping SSE chunk: 'choices' missing or empty -> {}", json);
                            return Flux.empty();
                        }

                        JsonNode contentNode = choices.get(0).path("delta").path("content");
                        if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                            return Flux.just(" " + contentNode.asText()); // the space before done is very important, that is how frontend is expecting
                        } else {
                            log.debug("Skipping SSE chunk: no content found -> {}", json);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse SSE chunk: {}", line, e);
                        return Flux.empty();
                    }
                    return Flux.empty();
                })
                .concatWith(Flux.just(" [DONE]")); // the space before done is very important, that is how frontend is expecting

    }

}
