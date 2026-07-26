
package com.budgetwise.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GroqClient {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GroqClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateContent(String prompt) {

        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();

        body.put("model", "llama-3.3-70b-versatile");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        messages.add(userMessage);

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            Map responseBody = response.getBody();

            List choices = (List) responseBody.get("choices");

            Map firstChoice = (Map) choices.get(0);

            Map message = (Map) firstChoice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}