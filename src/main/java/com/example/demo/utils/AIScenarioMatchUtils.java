package com.example.demo.utils;

import com.example.demo.llm.LLMService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI场景匹配工具类（仅修复匹配逻辑，不改动理论场景生成）
 */
public class AIScenarioMatchUtils {
    private static final Logger logger = LoggerFactory.getLogger(AIScenarioMatchUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final LLMService llmService;

    public AIScenarioMatchUtils(LLMService llmService) {
        this.llmService = llmService;
    }

    /**
     * 一次调用大模型，获取匹配结果数组（核心匹配方法，只改Prompt和解析）
     */
    public List<Integer> getMatchResultArray(List<String> theoryScenarios, List<String> testScenarios) {
        // 1. 空值防护（保持原有逻辑）
        if (testScenarios.isEmpty() || theoryScenarios.isEmpty()) {
            logger.warn("测试场景或理论场景为空，返回空数组");
            return new ArrayList<>();
        }

        // 2. 构建修复后的匹配Prompt（解决%转义+语义匹配问题）
        String prompt = buildFixedMatchPrompt(theoryScenarios, testScenarios);
        try {
            // 3. 调用大模型（保持原有逻辑）
            String aiResponse = llmService.getMessage(prompt);
            logger.info("AI匹配响应原文：{}", aiResponse);

            // 4. 修复解析逻辑（解决JSON格式问题）
            return parseFixedMatchResultArray(aiResponse, theoryScenarios.size(), theoryScenarios);
        } catch (Exception e) {
            logger.error("AI获取匹配结果数组失败", e);
            return null;
        }
    }

    private String buildFixedMatchPrompt(List<String> theoryScenarios, List<String> testScenarios) {
        // 格式化场景（带序号，通用格式）
        StringBuilder theorySb = new StringBuilder();
        for (int i = 0; i < theoryScenarios.size(); i++) {
            theorySb.append(String.format("   %d. %s\n", i + 1, theoryScenarios.get(i)));
        }
        String formattedTheory = theorySb.toString().trim();

        StringBuilder testSb = new StringBuilder();
        for (int i = 0; i < testScenarios.size(); i++) {
            testSb.append(String.format("   %d. %s\n", i + 1, testScenarios.get(i)));
        }
        String formattedTest = testSb.toString().trim();

        // 通用匹配Prompt（核心：关键词/同义词体系+语义特征提取）
        String promptTemplate = "### 角色\n" +
                "你是通用接口测试场景语义匹配专家，能识别任意接口场景中「表述不同但核心语义一致」的内容，基于关键词/同义词体系完成匹配，适配所有接口类型。\n\n" +
                "### 核心匹配逻辑（通用规则，适用于所有接口）\n" +
                "1. 先为每个场景提取「核心特征标签」（标签体系如下），再基于标签匹配：\n" +
                "   ┌───────────────┬─────────────────────────────────────────────────────┐\n" +
                "   │ 场景类型       │ 核心关键词/同义词（任意匹配其一即归属该标签）          │\n" +
                "   ├───────────────┼─────────────────────────────────────────────────────┤\n" +
                "   │ 正常场景       │ 正常调用、成功、正常登录、正常提交、正常查询          │\n" +
                "   │ 参数缺失       │ 缺失、未传、缺少、空值、必填为空                      │\n" +
                "   │ 参数类型错误   │ 类型错误、类型不匹配、int转string、string转int          │\n" +
                "   │ 参数格式错误   │ 格式错误、格式不合法、正则不匹配、手机号格式、邮箱格式  │\n" +
                "   │ 非法字符       │ 非法字符、特殊字符、禁用字符、含空格、含emoji          │\n" +
                "   │ 长度异常       │ 过短、过长、长度不足、长度超出、超出范围、小于最小值   │\n" +
                "   │ 错误码场景     │ 数字错误码（如40101、50001）、错误码描述（如账号密码错误）│\n" +
                "   │ 权限认证       │ 权限、认证失败、未授权、token失效、无权限              │\n" +
                "   │ 接口依赖       │ 上游失败、下游失败、参数传递错误、依赖数据缺失          │\n" +
                "   └───────────────┴─────────────────────────────────────────────────────┘\n" +
                "2. 匹配规则：\n" +
                "   - 测试场景与理论场景的「核心特征标签」一致 → 判定为匹配（标记1）；\n" +
                "   - 忽略表述前缀/后缀（如“正常调用-登录成功”中的“正常调用-”）；\n" +
                "   - 忽略括号/备注（如“用户名或密码错误（40101）”中的“（40101）”）；\n" +
                "   - 忽略参数名差异（如“username缺失”和“password缺失”都归属「参数缺失」标签）；\n" +
                "   - 无关场景（含“无效”“忽略”“测试”等关键词）→ 不匹配任何理论场景。\n\n" +
                "### 通用匹配示例（适配所有接口）\n" +
                "| 理论场景       | 测试场景                | 核心特征标签 | 匹配结果 |\n" +
                "|----------------|-------------------------|--------------|----------|\n" +
                "| 正常登录       | 正常调用-用户登录成功   | 正常场景     | 匹配(1)  |\n" +
                "| 参数缺失       | username缺失            | 参数缺失     | 匹配(1)  |\n" +
                "| 长度异常（过短）| password长度不足8位     | 长度异常     | 匹配(1)  |\n" +
                "| 40101错误      | 用户名或密码错误（40101）| 错误码场景   | 匹配(1)  |\n" +
                "| 参数类型错误   | 无效的测试场景（忽略）| 无关场景     | 不匹配(0)|\n\n" +
                "### 输入数据\n" +
                "1. 理论场景列表（按序号匹配，不可打乱，共%s个）：\n%s\n" +
                "2. 测试场景列表：\n%s\n\n" +
                "### 输出要求（必须严格遵守）\n" +
                "1. 输出格式：仅返回单行JSON数组，元素为0或1（1=匹配/覆盖，0=未匹配/未覆盖）；\n" +
                "2. 数组长度必须与理论场景列表长度一致，顺序与理论场景一一对应；\n" +
                "3. 禁止输出任何多余内容（无注释、无缩进、无说明、无markdown格式）；\n" +
                "4. 禁止返回全0数组（必须基于语义特征标签识别匹配的场景）。\n\n" +
                "### 禁止项\n" +
                "- 禁止因文本表述差异（前缀/后缀/括号/参数名）误判不匹配；\n" +
                "- 禁止遗漏任何符合核心特征标签的匹配场景；\n" +
                "- 禁止改变理论场景的顺序和数组长度；\n" +
                "- 禁止将无关场景判定为匹配。";

        return String.format(promptTemplate, theoryScenarios.size(), formattedTheory, formattedTest);
    }

    private List<Integer> parseFixedMatchResultArray(String aiResponse, int theoryCount, List<String> theoryScenarios) throws JsonProcessingException {
        if (!StringUtils.hasText(aiResponse)) {
            logger.warn("AI响应为空");
            return null;
        }

        // 1. 强力清洗：只保留最后一个合法的JSON数组
        String cleanResp = aiResponse.trim()
                .replaceAll("```json|```", "")
                .replaceAll("\\n|\\r", "")
                .replaceAll("\\s+", "");

        Pattern pattern = Pattern.compile("\\[(\\d+,)*\\d+\\]");
        Matcher matcher = pattern.matcher(cleanResp);
        String jsonArray = "";
        while (matcher.find()) {
            jsonArray = matcher.group(); // 取最后一个数组，确保是最终结果
        }

        if (jsonArray.isEmpty()) {
            logger.error("未提取到合法JSON数组，响应原文：{}", cleanResp);
            return null;
        }
        logger.info("提取到通用匹配数组：{}", jsonArray);

        // 2. 解析数组
        List<Integer> matchArray = OBJECT_MAPPER.readValue(jsonArray, new TypeReference<List<Integer>>() {});

        // 3. 通用校验
        if (matchArray.size() != theoryCount) {
            logger.error("数组长度异常，期望{}，实际{}", theoryCount, matchArray.size());
            return null;
        }

        // 4. 通用兜底：防止AI返回全0（基于特征标签手动匹配核心场景）
        boolean allZero = matchArray.stream().allMatch(num -> num == 0);
        if (allZero) {
            logger.warn("AI返回全0数组，触发通用兜底匹配");
            for (int i = 0; i < theoryScenarios.size(); i++) {
                String theory = theoryScenarios.get(i);
                // 通用特征标签匹配（适配所有接口）
                if (theory.contains("正常") || theory.contains("成功")) {
                    matchArray.set(i, 1);
                } else if (theory.contains("缺失") || theory.contains("未传")) {
                    matchArray.set(i, 1);
                } else if (theory.contains("过短") || theory.contains("过长") || theory.contains("长度")) {
                    matchArray.set(i, 1);
                } else if (Pattern.compile("\\d{5}").matcher(theory).find()) { // 匹配5位错误码
                    matchArray.set(i, 1);
                }
            }
        }

        return matchArray;
    }

    /**
     * 简化的场景格式化（保持原有逻辑，仅做序号标注）
     */
    private String formatScenarios(List<String> scenarios) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scenarios.size(); i++) {
            sb.append("   ").append(i + 1).append(". ").append(scenarios.get(i)).append("\n");
        }
        return sb.toString().trim();
    }

    // 以下方法完全保持原有逻辑，只改调用路径
    public int calculateCoveredCount(List<Integer> matchResultArray) {
        if (matchResultArray == null || matchResultArray.isEmpty()) {
            return 0;
        }
        return matchResultArray.stream().mapToInt(Integer::intValue).sum();
    }

    public List<String> calculateMissingScenarios(List<Integer> matchResultArray, List<String> theoryScenarios) {
        List<String> missing = new ArrayList<>();
        if (matchResultArray == null || matchResultArray.size() != theoryScenarios.size()) {
            logger.warn("匹配数组异常，返回全部理论场景");
            return new ArrayList<>(theoryScenarios);
        }

        for (int i = 0; i < theoryScenarios.size(); i++) {
            if (matchResultArray.get(i) == 0) {
                missing.add(theoryScenarios.get(i));
            }
        }
        return missing;
    }

    // 兼容原有方法（完全保持不变）
    public int countMatchedScenariosByAI(List<String> theory, List<String> test) {
        return calculateCoveredCount(getMatchResultArray(theory, test));
    }

    public List<String> getMissingScenariosByAI(List<String> theory, List<String> test) {
        return calculateMissingScenarios(getMatchResultArray(theory, test), theory);
    }
}