package com.jiangmai.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeepSeekService {

    @Value("${ai.provider:openai}")
    private String provider;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${doubao.api.key:}")
    private String doubaoApiKey;

    @Value("${doubao.api.url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String doubaoApiUrl;

    @Value("${doubao.api.model:doubao-seed-1-6-250615}")
    private String doubaoModel;

    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/chat/completions}")
    private String deepseekApiUrl;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String deepseekModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String userMessage) {
        try {
            String selectedProvider = resolveProvider();
            String apiKey;
            String apiUrl;
            String model;
            if (selectedProvider.equals("openai")) {
                apiKey = openaiApiKey;
                apiUrl = openaiApiUrl;
                model = openaiModel;
            } else if (selectedProvider.equals("doubao")) {
                apiKey = doubaoApiKey;
                apiUrl = doubaoApiUrl;
                model = doubaoModel;
            } else {
                apiKey = deepseekApiKey;
                apiUrl = deepseekApiUrl;
                model = deepseekModel;
            }

            if (apiKey == null || apiKey.isBlank()) {
                if (selectedProvider.equals("openai")) {
                    return "AI 服务未配置 OPENAI_API_KEY，请联系管理员配置。";
                }
                if (selectedProvider.equals("doubao")) {
                    return "AI 服务未配置 ARK_API_KEY，请联系管理员配置。";
                }
                return "AI 服务未配置 DEEPSEEK_API_KEY，请联系管理员配置。";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.7);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一位资深的非遗锡雕专家与辅学老师，负责为平台上学习和创作的学员解答关于锡雕的历史、工艺技术、工具使用等相关问题。如果用户询问其他无关话题，请委婉地引回锡雕或手工艺领域。");
            
            Map<String, String> userMsgObj = new HashMap<>();
            userMsgObj.put("role", "user");
            userMsgObj.put("content", userMessage);

            messages.add(systemMsg);
            messages.add(userMsgObj);
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    return message.get("content");
                }
            } else {
                log.error("{} API request failed: HTTP {} - {}", selectedProvider, response.getStatusCode(), response.getBody());
                return "抱歉，由于网络或接口原因，AI暂时无法给您答复，请稍后再试。";
            }
        } catch (Exception e) {
            log.error("Error calling AI API", e);
            return "抱歉，系统内部发生错误，无法连接到AI服务。";
        }
        return "未能生成回复，请重新提问。";
    }

    private String resolveProvider() {
        String normalized = provider == null ? "" : provider.trim().toLowerCase();
        if (normalized.equals("openai") || normalized.equals("gpt")) {
            return "openai";
        }
        if (normalized.equals("doubao")) {
            return "doubao";
        }
        if (normalized.equals("deepseek")) {
            return "deepseek";
        }
        if (doubaoApiKey != null && !doubaoApiKey.isBlank()) {
            return "doubao";
        }
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            return "openai";
        }
        return "deepseek";
    }
}
