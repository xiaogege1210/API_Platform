package com.example.demo.service.impl;

import com.example.demo.llm.LLMService;
import com.example.demo.service.ApiSceneAnalyzerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 理论接口场景提取服务（完全对齐你的Prompt构建风格）
 */
@Service
public class ApiSceneAnalyzerServiceImpl implements ApiSceneAnalyzerService {

    @Autowired
    private LLMService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[.*?\\]", Pattern.DOTALL);

    @Override
    public List<String> analyze(String apiDoc,String extraScene) {
        if (!StringUtils.hasText(apiDoc)) {
            return new ArrayList<>();
        }

        try {
            // 构建和你风格完全一致的Prompt
            String prompt = buildStandardPrompt(apiDoc,extraScene);
            System.out.println(prompt);
            String llmResponse = llmService.getMessage(prompt);
            return parseSceneList(llmResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 完全对齐你风格的Prompt构建方法（角色+输入数据+核心任务+输出要求+禁止项）
     */
    private String buildStandardPrompt(String apiDoc,String extraScene) {
        // 1. 基础参数转义（和你代码保持一致的转义逻辑）
        String escapedApi = escapeAndTruncate(apiDoc, 8000);

        // 2. 构建Prompt（完全复刻你的风格：结构化、无冗余、强约束）
        StringBuilder promptBuilder = new StringBuilder(10000); // 预设容量，减少扩容
        promptBuilder.append("### 角色\n")
                .append("你是资深接口测试专家，需基于【API文档】提取该接口需覆盖的所有理论测试场景，输出JSON格式场景列表，无任何多余内容。\n\n")
// 输入数据（和你代码结构一致）
                .append("### 输入数据\n")
                .append("1. API文档：接口入参/出参规范、请求方式、异常码、边界值、权限认证要求\n")
                .append("   内容：").append(escapedApi).append("\n\n")
                .append("2. 接口场景描述：").append(extraScene).append("\n\n")
// 核心分析任务（和你代码结构一致，分点明确）
                .append("### 核心分析任务\n")
                .append("1. 全维度场景提取（无遗漏）：\n")
                .append("   - 正向场景：正常调用（单数据/多数据）、参数默认值、全参数合法调用\n")
                .append("   - 异常入参场景：必填参数缺失/为空、参数类型不匹配、格式错误、含非法字符;注意：需列出详细参数名称的异常\n")
                .append("   - 边界值场景：数值型参数等于/超出最小/最大值、字符串等于/超出最小/最大长度，请结合具体的参数名称给出异常场景\n")
                .append("   - 错误码场景：API文档中所有异常码的触发场景\n")
                .append("   - 权限认证场景：如果有的话请给出\n")
                .append("   - 接口依赖场景：上下游接口调用失败、参数传递错误、依赖数据缺失\n")
                .append("2. 场景标准化处理：\n")
                .append("   - 每个场景描述简洁明确（≤20字）\n")
                .append("   - 无重复场景、无冗余描述，每个场景仅描述一个核心测试点\n")
                .append("   - 场景命名统一，避免模糊表述\n")
// 输出要求（强约束，和你代码风格一致）
                .append("### 输出要求\n")
                .append("1. 格式约束：仅输出单行JSON数组字符串，无缩进、无换行、无注释，空值填空字符串\n")
                .append("2. 场景字段要求：\n")
                .append("   - 每个元素为场景描述字符串（≤20字），严格匹配核心分析任务的维度\n")
                .append("   - 数组元素按场景维度分类排序（正向→异常→边界值→错误码→权限→依赖）\n")
                .append("   - 无重复元素、无空元素、无无关元素（如不包含测试环境相关内容）\n")
//                .append("3. 输出示例：\n")
//                .append("[\"正常调用-单数据\",\"user_id缺失\",\"user_id为负数\",\"user_id超出最大值\",\"Token失效\",\"错误码40001触发\"]\n\n")
// 禁止项（强化约束，和你代码风格一致）
                .append("### 禁止项\n")
                .append("- 禁止输出JSON以外的任何内容（包括说明、注释、缩进、换行、示例解释）\n")
                .append("- 禁止遗漏API文档中的核心场景（如边界值、错误码、权限认证）\n")
                .append("- 禁止场景描述模糊（如必须明确参数名，不用\"参数错误\"等笼统表述）\n")
                .append("- 禁止重复场景（如同一语义，但不同表达方式的场景）\n")
                .append("- 禁止添加测试环境、执行步骤等无关内容到场景描述中");

        // 最终长度校验（和你代码保持一致的逻辑）
        String finalPrompt = promptBuilder.toString();
        if (finalPrompt.length() > 10000) {
            finalPrompt = finalPrompt.substring(0, 9997) + "...";
        }
        return finalPrompt;
    }

    /**
     * 和你代码完全一致的转义+截断方法
     */
    private String escapeAndTruncate(String content, int maxLen) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 转义核心特殊字符（避免破坏JSON/Prompt结构）
        String escaped = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\t", " ");
        // 长度截断（保留末尾标识）
        if (escaped.length() > maxLen) {
            return escaped.substring(0, maxLen - 3) + "...";
        }
        return escaped;
    }

    /**
     * 解析大模型返回的场景列表
     */
    private List<String> parseSceneList(String llmResponse) {
        try {
            // 提取JSON数组部分
            Matcher matcher = JSON_ARRAY_PATTERN.matcher(llmResponse);
            if (!matcher.find()) {
                return new ArrayList<>();
            }
            String jsonArray = matcher.group();

            // 解析并清理场景列表
            List<String> scenes = objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
            return scenes.stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());  // 修改这里

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}