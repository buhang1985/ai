package com.gjyy.ai.ragflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RagflowClient {

    private final RestTemplate restTemplate; // 用于发送HTTP请求的RestTemplate
    private final ObjectMapper objectMapper; // 用于JSON处理的ObjectMapper
    // 从配置文件中读取RagFlow基础URL
    @Value("${ragflow.base-url}")
    private String baseUrl;
    // 从配置文件中读取RagFlow API基础URL
    @Value("${ragflow.api-url}")
    private String apiUrl;
    // 从配置文件中读取RagFlow API密钥
    @Value("${ragflow.api-key}")
    private String apiKey;

    @Autowired
    public RagflowClient(RestTemplate restTemplate,ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    /**
     * 搜索知识库
     *
     * @param kbId            知识库ID
     * @param query           查询文本
     * @param topK            返回结果数量
     * @param scoreThreshold  相似度得分阈值
     * @param includeMetadata 是否包含元数据
     * @return 检索结果列表
     */
    public List<Map<String, Object>> searchKnowledgeBase(String kbId, String query, int topK, double scoreThreshold, boolean includeMetadata) {
        // RagFlow搜索API端点
        String endpoint = "http://10.192.23.250:8888/api/v1/search";
        // 创建HTTP请求头
        HttpHeaders headers = new HttpHeaders();
        // 设置授权头
        headers.set("Authorization", "Bearer " + apiKey);
        // 设置内容类型为JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 创建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("top_k", topK);
        payload.put("score_threshold", scoreThreshold);
        payload.put("include_metadata", includeMetadata);
        payload.put("dataset_ids", List.of(kbId));
        payload.put("document_ids", new ArrayList<>());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            // 发送POST请求
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, JsonNode.class);
            // 获取响应体
            JsonNode responseBody = responseEntity.getBody();
            // 检查响应体是否包含数据
            if (responseBody != null && responseBody.has("data")) {
                // 获取数据
                JsonNode data = responseBody.get("data");
                // 格式化搜索结果
                return formatSearchResults(data);
            } else {
                // 返回空列表
                return new ArrayList<>();
            }
        } catch (Exception e) {
            // 打印错误信息
            System.out.println("API请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    /**
     * 检索知识库
     *
     * @param kbId            知识库ID
     * @param dsId            数据集ID
     * @param query           查询文本
     * @param topK            返回结果数量
     * @param scoreThreshold  相似度得分阈值
     * @param includeMetadata 是否包含元数据
     * @return 检索结果列表
     */
    public List<Map<String, Object>> retrievalKnowledgeBase(String kbId, String dsId, String query, int topK, double scoreThreshold, boolean includeMetadata) {
        // RagFlow检索API端点
        String endpoint = apiUrl + "/retrieval";
        // 创建HTTP请求头
        HttpHeaders headers = new HttpHeaders();
        // 设置授权头
        headers.set("Authorization", "Bearer " + apiKey);
        // 设置内容类型为JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 创建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("top_k", topK);
        payload.put("score_threshold", scoreThreshold);
        payload.put("include_metadata", includeMetadata);
        payload.put("question", query);
        payload.put("dataset_ids", List.of(dsId));
        payload.put("document_ids", new ArrayList<>());
        payload.put("page_size", 10);
        payload.put("similarity_threshold", 0.3);
        payload.put("highlight", "false");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            // 发送POST请求
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, JsonNode.class);
            // 获取响应体
            JsonNode responseBody = responseEntity.getBody();
            // 检查响应体是否包含数据
            if (responseBody != null && responseBody.has("data")) {
                // 获取数据
                JsonNode data = responseBody.get("data");
                // 格式化搜索结果
                return formatRetrievalResults(data);
            } else {
                // 返回空列表
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("API请求失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    /**
     * 格式化搜索结果
     *
     * @param results 原始搜索结果
     * @return 格式化后的搜索结果列表
     */
    private List<Map<String, Object>> formatSearchResults(JsonNode results) {
        // 创建格式化后的结果列表
        List<Map<String, Object>> formatted = new ArrayList<>();
        // 检查结果是否为数组
        if (results != null && results.isArray()) {
            // 遍历结果数组
            for (JsonNode item : results) {
                // 创建格式化后的结果项
                Map<String, Object> formattedItem = new HashMap<>();
                // 获取文本内容
                formattedItem.put("content", item.has("text") ? item.get("text").asText() : "");
                // 获取来源
                formattedItem.put("source", item.has("metadata") && item.get("metadata").has("source") ? item.get("metadata").get("source").asText() : "unknown");
                // 获取得分
                formattedItem.put("score", item.has("score") ? Math.round(item.get("score").asDouble() * 10000.0) / 10000.0 : 0.0);
                // 添加到格式化后的结果列表
                formatted.add(formattedItem);
            }
        }
        // 返回格式化后的结果列表
        return formatted;
    }
    /**
     * 格式化检索结果
     *
     * @param results 原始检索结果
     * @return 格式化后的检索结果列表
     */
    private List<Map<String, Object>> formatRetrievalResults(JsonNode results) {
        // 创建格式化后的结果列表
        List<Map<String, Object>> formatted = new ArrayList<>();
        // 检查结果是否包含chunks数组
        if (results != null && results.has("chunks") && results.get("chunks").isArray()) {
            // 遍历chunks数组
            for (JsonNode chunk : results.get("chunks")) {
                // 创建格式化后的结果项
                Map<String, Object> formattedChunk = new HashMap<>();
                // 获取文本内容
                formattedChunk.put("content", chunk.has("content") ? chunk.get("content").asText() : "");
                // 获取文档关键字
                formattedChunk.put("document", chunk.has("document_keyword") ? chunk.get("document_keyword").asText() : "");
                // 获取id
                formattedChunk.put("id", chunk.has("id") ? chunk.get("id").asText() : "");
                // 获取知识库id
                formattedChunk.put("kb_id", chunk.has("kb_id") ? chunk.get("kb_id").asText() : "");
                formattedChunk.put("similarity", chunk.has("similarity") ? chunk.get("similarity").asDouble() : 0.0);
                formattedChunk.put("term_similarity", chunk.has("term_similarity") ? chunk.get("term_similarity").asDouble() : 0.0);
                formattedChunk.put("vector_similarity", chunk.has("vector_similarity") ? chunk.get("vector_similarity").asDouble() : 0.0);
                formattedChunk.put("score", chunk.has("similarity") ? Math.round(chunk.get("similarity").asDouble() * 10000.0) / 10000.0 : 0.0);
                formatted.add(formattedChunk);
            }
        }
        return formatted;
    }
}