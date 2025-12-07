package com.example.demo.service.impl;

import com.example.demo.model.GenerateCodeRequest;
import com.example.demo.service.ExternalTestCaseGeneratorService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * 外部测试用例生成服务实现类
 */
@Service
public class ExternalTestCaseGeneratorServiceImpl implements ExternalTestCaseGeneratorService {

    @Override
    public String generateTestCase(List<GenerateCodeRequest> request) {
        try {
            GenerateCodeRequest generateCodeRequest=request.get(0);
            RestTemplate restTemplate = new RestTemplate();

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 创建请求实体
            HttpEntity<GenerateCodeRequest> entity = new HttpEntity<>(generateCodeRequest, headers);

            // 调用 FastAPI 接口
            String response = restTemplate.postForObject(
                    "http://0.0.0.0:8000/generate-test-cases",
                    entity,
                    String.class
            );

            return response;

        } catch (Exception e) {
            throw new RuntimeException("调用测试用例生成服务失败: " + e.getMessage(), e);
        }
    }
}