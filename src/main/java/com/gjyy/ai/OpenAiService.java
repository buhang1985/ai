package com.gjyy.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service
public class OpenAiService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);
    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public OpenAiService(OpenAiConfig openAiConfig, RestTemplate restTemplate) {
        this.openAiConfig = openAiConfig;
        this.restTemplate = restTemplate;
    }

    public String callChatApi(String prompt) {
        String apiUrl = openAiConfig.getApiUrl();
        String apiKey = openAiConfig.getApiKey();
        logger.info("API URL: {}", apiUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey); // 假设 API 需要 Bearer 认证
        logger.info("Request Headers: {}", headers);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "DeepSeek-R1-Distill-Llama-70"); // 替换为你的模型名称
        requestBody.put("messages", java.util.List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                // 根据你的 API 响应格式进行解析
                java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) responseEntity.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            return "Error calling API or parsing response.";
        } catch (Exception e) {
            return "Error calling API: " + e.getMessage();
        }
    }
}