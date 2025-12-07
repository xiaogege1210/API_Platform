package com.example.demo.service.impl;

import com.example.demo.llm.LLMService;
import com.example.demo.service.TestCaseAnalysisService;
import com.example.demo.utils.LLMJsonValidator; // 导入我们创建的工具类
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestCaseAnalysisServiceImpl implements TestCaseAnalysisService {

    @Autowired
    private LLMService llmService;

    @Override
    public String generateAnalysisAndSuggestions(String api, String environment, String dependency, String test, String testResult)
            throws JsonProcessingException {

        // 1. 构建 Prompt
        String prompt = buildStandardPrompt(api, environment, dependency, test, testResult);

        // 2. 调用 LLM
        String llmRawOutput = llmService.getMessage(prompt);

        System.out.println("LLM 原始输出：");
        System.out.println(llmRawOutput);

        // 3. 使用 JSON 校验与修复工具
        String safeJson = LLMJsonValidator.validateAndFixJson(
                llmRawOutput,
                LLMJsonValidator.getDefaultFallbackJson()
        );

        System.out.println("校验后的安全 JSON：");
        System.out.println(safeJson);

        return safeJson;
    }

    // =====================================
    // 以下为你的原有方法（未改动）
    // =====================================

    private String buildStandardPrompt(String api, String environment, String dependency, String test, String testResult) {
        String escapedApi = escapeAndTruncate(api, 5000);
        String escapedScenarios = escapeAndTruncate(environment, 2000);
        String escapedDep = escapeAndTruncate(dependency, 2000);
        String escapedTest = escapeAndTruncate(test, 30000);
        String escapedResult = escapeAndTruncate(testResult, 5000);

        StringBuilder sb = new StringBuilder(200000);

        sb.append(
                "### 角色\n" +
                        "你是自动化测试专家，需要基于【给定测试场景】+ API 文档 + 接口依赖，对测试脚本进行分析并输出结构化优化建议。务必输出的是完整的json格式\n\n" +
                        "### 输入数据\n" +
                        "1. 给定测试场景：\n" + escapedScenarios + "\n" +
                        "2. API 文档：\n" + escapedApi + "\n" +
                        "3. 接口依赖：\n" + escapedDep + "\n" +
                        "4. 测试脚本：\n" + escapedTest + "\n" +
                        "5. 执行结果：\n" + escapedResult + "\n\n" +
                        "### 核心分析任务\n" +
                        "1. 场景覆盖分析：识别未覆盖场景、冗余场景，并说明原因。\n" +
                        "2. 脚本正确性分析：检查参数、调用方式、依赖顺序、断言完整性。\n" +
                        "3. 执行失败分析：定位失败根因，给出可执行修复方案（含代码）。\n\n" +
                        "### 输出要求\n" +
                        "- 必须输出 **合法 JSON**，允许换行，但不能输出 JSON 以外内容。\n" +
                        "- JSON 必须以 { 开头，以 } 结束。\n" +
                        "- 建议数量最多 3 条。\n" +
                        "- 严格遵守字段长度限制。\n\n" +
                        "{\n" +
                        "  \"metrics\": {\n" +
                        "    \"totalLines\": 整数,\n" +
                        "    \"testMethodCount\": 整数,\n" +
                        "    \"assertionCount\": 整数,\n" +
                        "    \"coverageScore\": 0-100 的整数\n" +
                        "  },\n" +
                        "  \"suggestions\": [\n" +
                        "    {\n" +
                        "      \"id\": \"S001\",\n" +
                        "      \"priority\": \"high/medium/low\",\n" +
                        "      \"dimension\": \"场景覆盖/脚本正确性/执行失败\",\n" +
                        "      \"problem_desc\": \"≤40 字\",\n" +
                        "      \"modify_operation\": \"≤30 字\",\n" +
                        "      \"modify_example\": \"≤100 字\",\n" +
                        "      \"value\": \"≤20 字\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n"
        );

        return sb.toString();


    }

    private String escapeAndTruncate(String content, int maxLen) {
        if (content == null || content.isEmpty()) return "";
        String escaped = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\t", " ");
        if (escaped.length() > maxLen) {
            return escaped.substring(0, maxLen - 3) + "...";
        }
        return escaped;
    }
}

