package com.example.demo.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LLMJsonValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 校验 LLM 输出是否是合法 JSON，并尝试修复常见结尾缺失
     * @param rawOutput LLM 原始输出
     * @param fallback 安全的 fallback JSON（必须是合法 JSON）
     * @return 合法 JSON 字符串
     */
    public static String validateAndFixJson(String rawOutput, String fallback) {
        if (rawOutput == null || rawOutput.isEmpty()) {
            return fallback;
        }

        // 尝试直接解析
        try {
            objectMapper.readTree(rawOutput);
            return rawOutput; // 已经合法
        } catch (Exception e) {
            // 尝试截取最外层 JSON 对象
            int firstBrace = rawOutput.indexOf('{');
            int lastBrace = rawOutput.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                String candidate = rawOutput.substring(firstBrace, lastBrace + 1);
                try {
                    objectMapper.readTree(candidate);
                    return candidate;
                } catch (Exception ex) {
                    // 截取失败，返回 fallback
                    return fallback;
                }
            } else {
                return fallback;
            }
        }
    }

    /**
     * 校验 JSON 是否有效
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) return false;
        try {
            JsonNode node = objectMapper.readTree(json);
            return node != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 示例 fallback JSON
     */
    public static String getDefaultFallbackJson() {
        return "{\n" +
                "  \"metrics\": {\"totalLines\":0, \"testMethodCount\":0, \"assertionCount\":0, \"coverageScore\":0},\n" +
                "  \"suggestions\": [],\n" +
                "  \"summary\": {\"total_suggestions\":0, \"high_priority_count\":0, \"core_problem\":\"无数据\"}\n" +
                "}";
    }
}
