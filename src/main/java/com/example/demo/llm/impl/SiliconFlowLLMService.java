package com.example.demo.llm.impl;

import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.RequestBodyDTO;
import com.example.demo.llm.LLMService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;


import java.util.*;

@Service
public class SiliconFlowLLMService implements LLMService {
    private static final Logger logger = LoggerFactory.getLogger(SiliconFlowLLMService.class);

    private final RestTemplate restTemplate;

    @Value( "${llm.api.url}")
    private String apiUrl;

    @Value("${llm.api.token}")
    private String apiToken;


    @Value("${llm.api.model}")
    private String model;

    public SiliconFlowLLMService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    private static int MAX_CONTENT_LENGTH=4000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getMessage(String query) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiToken);
        String processedQuery = processQuery(query);

        // 3. 构建请求体 Map（结构化，避免拼接错误）
        RequestBodyDTO requestBodyDTO = new RequestBodyDTO();
        requestBodyDTO.setModel(model.trim()); // 去除首尾空格
        requestBodyDTO.setMax_tokens(512);
        requestBodyDTO.setTemperature(0.7);
        requestBodyDTO.setStream(false);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setRole("user"); // 固定为 user，不能修改
        messageDTO.setContent(processedQuery);
        requestBodyDTO.setMessages(new MessageDTO[]{messageDTO});

        // 4. 序列化（Jackson 自动处理所有转义，避免手动拼接错误）
        String requestJson = OBJECT_MAPPER.writeValueAsString(requestBodyDTO);
        logger.info("SiliconFlow 请求体：{}", requestJson);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl,        // 接口地址
                requestEntity, // 请求头+请求体
                String.class   // 响应体类型
        );
        return  extractTextFromJsonResponse(response.getBody());


    }

    /**
     *
     * @param query
     * @return
     */
    private static String processQuery(String query) {
        return query
                .replace("\\", "\\\\")   // 转义反斜杠
                .replace("\"", "\\\"")  // 转义双引号
                .replace("\n", "\\n")    // 转义换行符
                .replace("\r", "\\r")    // 转义回车符
                .replace("\t", "\\t");   // 转义制表符
    }
    /**
     * 从 JSON 响应中提取文字内容（使用 Spring 的 JsonParser）
     */
    private String extractTextFromJsonResponse(String jsonResponse) {
        // 使用 Spring 的 JsonParser
        org.springframework.boot.json.JsonParser parser =
                org.springframework.boot.json.JsonParserFactory.getJsonParser();
        Map<String, Object> responseMap = parser.parseMap(jsonResponse);

        // 提取 choices 数组
        List<Object> choices = (List<Object>) responseMap.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> firstChoice = (Map<String, Object>) choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            if (message != null) {
                String content = (String) message.get("content");
                if (content != null) {
                    return content;
                }
            }
        }

        // 尝试直接获取 content
        String content = (String) responseMap.get("content");
        if (content != null) {
            return content;
        }

        logger.warn("无法从响应中提取内容，返回原始响应");
        return jsonResponse;


    }


}