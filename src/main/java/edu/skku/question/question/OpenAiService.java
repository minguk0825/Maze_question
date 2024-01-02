// OpenAiService.java
package edu.skku.question.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final RestTemplate restTemplate;
    private final String openAiUrl;

    @Value("${OPENAIKEY}")
    private String apiKey;

    public OpenAiService(@Value("${OPENAI_URL}") String openAiUrl) {
        this.restTemplate = new RestTemplate();
        this.openAiUrl = openAiUrl;
    }

    @SneakyThrows
    public String generateQuestion(String prompt) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(
                Map.of(
                        "model", "gpt-3.5-turbo",
                        "messages", List.of(Map.of("role", "system", "content", prompt)),
                        "max_tokens", 500
                )
        );

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
        System.out.println("Sending request to OpenAI: " + jsonRequest);
        ResponseEntity<String> response = restTemplate.exchange(
                openAiUrl, HttpMethod.POST, entity, String.class);
        System.out.println("Response from OpenAI: " + response.getBody());
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            // 에러 처리
            throw new RuntimeException("Failed to call OpenAI API: " + response.getStatusCode());
        }
    }
}
