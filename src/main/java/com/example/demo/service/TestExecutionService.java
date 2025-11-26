package com.example.demo.service;

import com.example.demo.model.GenerateCodeRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.demo.service.TenantTokenService;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestExecutionService {
    @Autowired
    private TenantTokenService tenantTokenService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    // 飞书 API 基础地址
    private static final String BASE_URL = "https://open.feishu.cn";
    // 获取 Token 的地址 (自建应用)
    private static final String TOKEN_URL = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

    // --- 用户定义的默认值 ---
    private static final String DEFAULT_RECEIVE_ID = "ou_8eb86e3b62d94dfcae09efe8caa90b66"; // 你的默认ID


    /**
     * 第二步：执行测试用例
     */
    public String executeTestCase(String appId, String appSecret, GenerateCodeRequest.ApiTask task, GenerateCodeRequest.TestScenario scenario) {
        StringBuilder logs = new StringBuilder();
        logs.append("=== 开始执行: ").append(scenario.getScenarioName()).append(" ===\n");

        try {
            // 1. 自动获取 Token
            String token = tenantTokenService.getTenantAccessToken();
            logs.append("✅ 1. 鉴权成功, Token: t-***").append(token.substring(token.length() - 6)).append("\n");

            // 2. 准备 URL 和 参数
            String url = BASE_URL + task.getApiPath();
            Map<String, Object> bodyParams = new HashMap<>();
            StringBuilder queryString = new StringBuilder();

            // 遍历用户输入的参数
            Map<String, GenerateCodeRequest.ParamValueInfo> userParams = scenario.getParamValues();
            if (userParams != null) {
                for (Map.Entry<String, GenerateCodeRequest.ParamValueInfo> entry : userParams.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().getValue();
                    String type = entry.getValue().getType();

                    // --- 核心逻辑：默认值处理 ---
                    if ("receive_id".equals(key) && (value == null || value.trim().isEmpty())) {
                        value = DEFAULT_RECEIVE_ID; // 使用默认 ID
                        logs.append("ℹ️ 使用默认 receive_id: ").append(value).append("\n");
                    }

                    // 如果值依然为空且不是必须参数，跳过
                    if (value == null || value.trim().isEmpty()) continue;

                    // --- 核心逻辑：参数位置判断 ---

                    // A. 处理 Path 参数 (如 /chats/{chat_id})
                    if (url.contains("{" + key + "}")) {
                        url = url.replace("{" + key + "}", value);
                    }
                    // B. 飞书特殊规则：receive_id_type 通常放在 URL Query 中
                    else if ("receive_id_type".equals(key) || "GET".equalsIgnoreCase(task.getMethod())) {
                        if (queryString.length() == 0) queryString.append("?");
                        else queryString.append("&");
                        queryString.append(key).append("=").append(value);
                    }
                    // C. 其他参数放入 Body (仅限 POST/PUT)
                    else {
                        // 特殊处理：content 字段在飞书中通常要求是 JSON 字符串
                        if ("content".equals(key)) {
                            // 如果用户输入的是 {"text":"hi"}，我们直接用，不需要再转义一次，因为 Gson 会处理
                            // 如果代码里是 builder.content("{\"text\":\"...\"}")，这里直接把字符串放进去即可
                            bodyParams.put(key, value);
                        } else {
                            bodyParams.put(key, value);
                        }
                    }
                }
            }

            // 拼接完整 URL
            String finalUrl = url + queryString.toString();
            logs.append("🔗 2. 请求 URL: ").append(task.getMethod()).append(" ").append(finalUrl).append("\n");
            if (!bodyParams.isEmpty()) {
                logs.append("📦 3. 请求 Body: ").append(gson.toJson(bodyParams)).append("\n");
            }

            // 3. 构造 HTTP 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token); // 自动添加 Authorization: Bearer <token>
            headers.setContentType(MediaType.APPLICATION_JSON); // Content-Type

            HttpEntity<Object> requestEntity = new HttpEntity<>(bodyParams, headers);

            // 4. 发送请求
            ResponseEntity<String> response;
            if ("POST".equalsIgnoreCase(task.getMethod())) {
                response = restTemplate.postForEntity(finalUrl, requestEntity, String.class);
            } else if ("GET".equalsIgnoreCase(task.getMethod())) {
                // GET 请求没有 Body
                response = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            } else {
                return "暂不支持该方法: " + task.getMethod();
            }

            // 5. 解析结果
            logs.append("📥 4. 响应状态: ").append(response.getStatusCodeValue()).append("\n");

            // 尝试格式化 JSON 输出
            try {
                JsonObject respJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
                logs.append("📄 5. 响应内容: \n").append(gson.toJson(respJson));
            } catch (Exception e) {
                logs.append("📄 5. 响应内容: ").append(response.getBody());
            }

        } catch (Exception e) {
            logs.append("❌ 执行出错: ").append(e.getMessage());
            e.printStackTrace();
        }

        return logs.toString();
    }
}