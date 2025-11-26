package com.example.demo.service;

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
public class TenantTokenService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取飞书 Tenant Access Token
     * @return Token 字符串
     */
    public String getTenantAccessToken() {
        String tokenUrl = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal/";
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("app_id", "cli_a9a8da6dd1f8dcc1");
        requestBody.put("app_secret", "KyFKZbyKAFj4iJMiXNrnWofRozLiObp7");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.get("tenant_access_token") != null) {
            return responseBody.get("tenant_access_token").toString();
        } else {
            throw new RuntimeException("获取飞书Token失败：" + responseBody);
        }
    }
}