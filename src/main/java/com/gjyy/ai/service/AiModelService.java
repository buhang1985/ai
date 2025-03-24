package com.gjyy.ai.service;

import com.gjyy.ai.OpenAiConfig;
import com.gjyy.ai.ragflow.RagflowClient;
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
import java.util.function.Consumer;

@Service
public class AiModelService {
    private static final Logger logger = LoggerFactory.getLogger(AiModelService.class);
    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;
    private final RagflowClient ragflowClient;

    @Autowired
    public AiModelService(OpenAiConfig openAiConfig, RestTemplate restTemplate, RagflowClient ragflowClient) {
        this.openAiConfig = openAiConfig;
        this.restTemplate = restTemplate;
        this.ragflowClient = ragflowClient;
    }

    public void chat(String prompt, Consumer<String> chunkConsumer) {
        String knowledge = ""; //ragflowClient.searchKnowledgeBase();
        String enhancedPrompt = "Knowledge: " + knowledge + "\nUser Query: " + prompt;

        String apiUrl = openAiConfig.getApiUrl();
        String apiKey = openAiConfig.getApiKey();
        logger.info("API URL: {}", apiUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "DeepSeek-R1-Distill-Llama-70");
        requestBody.put("messages", java.util.List.of(Map.of("role", "user", "content", prompt)));
        requestBody.put("stream", true); // 启用流式传输
        requestBody.put("temperature",0.3);
        requestBody.put("max_tokens",8012);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 使用 exchange 方法，以便处理流式响应
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                // 处理流式响应，根据您的 API 返回的流式数据格式进行解析
                String[] lines = responseEntity.getBody().split("\n");
                for (String line : lines) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (!data.equals("[DONE]")) {
                            // 假设 API 返回的流式数据是 JSON 格式的，需要解析
                            try {
                                // 简单的示例，您需要根据实际的 JSON 结构进行调整
                                Map<String, Object> json = new com.fasterxml.jackson.databind.ObjectMapper().readValue(data, Map.class);
                                java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) json.get("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                    if (delta != null && delta.containsKey("content")) {
                                        chunkConsumer.accept((String) delta.get("content"));
                                    }
                                }
                            } catch (com.fasterxml.jackson.databind.JsonMappingException e){
                                logger.error("json mapping error:{}",data);
                            } catch (Exception e) {
                                logger.error("Error parsing JSON: {}", e.getMessage());
                            }
                        }
                    }
                }
            } else {
                chunkConsumer.accept("Error calling API or parsing response.");
            }
        } catch (Exception e) {
            logger.error("Error calling API: {}", e.getMessage());
            chunkConsumer.accept("Error calling API: " + e.getMessage());
        }
    }
}