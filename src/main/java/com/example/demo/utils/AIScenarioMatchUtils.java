package com.example.demo.utils;

import com.example.demo.llm.LLMService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI场景匹配工具类（基于大模型语义理解）
 */
public class AIScenarioMatchUtils {
    private static final Logger logger = LoggerFactory.getLogger(AIScenarioMatchUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final LLMService llmService;

    // 注入已有的LLM服务（复用SiliconFlow调用逻辑）
    public AIScenarioMatchUtils(LLMService llmService) {
        this.llmService = llmService;
    }

    /**
     * AI批量匹配：计算测试场景覆盖的理论场景数
     * @param testScenarios 生成的测试场景列表
     * @param theoryScenarios 理论场景列表
     * @return 匹配成功的理论场景数
     */
    public int countMatchedScenariosByAI( List<String> theoryScenarios, List<String> testScenarios) {
        if (testScenarios.isEmpty() || theoryScenarios.isEmpty()) {
            logger.warn("测试场景或理论场景为空，无需匹配");
            return 0;
        }

        // 构建AI匹配Prompt（明确要求结构化输出）
        String prompt = buildMatchPrompt( theoryScenarios,testScenarios);
        try {
            // 调用大模型获取匹配结果
            String aiResponse = llmService.getMessage(prompt);
            logger.info("AI匹配响应：{}", aiResponse);

            // 解析AI返回的JSON结果
            return parseAiMatchResult(aiResponse, theoryScenarios.size());
        } catch (Exception e) {
            logger.error("AI场景匹配失败，降级为手动规则匹配", e);
//            // 降级策略：匹配失败时使用之前的手动规则，避免影响整体流程
//            return ScenarioMatchUtils.countMatchedScenarios(testScenarios, theoryScenarios);
        }
        return 0;
    }

    /**
     * 构建AI匹配的Prompt（关键：明确约束和输出格式）
     */
    private String buildMatchPrompt( List<String> theoryScenarios, List<String> testScenarios) {
        return String.format("### 任务\n你是语义匹配专家，帮我分析理论场景和测试场景中语义一致的场景（理论场景和测试场景中可能对同一场景的表达不一致）。\n\n### 规则\n1. 语义一致判断标准：测试场景和理论场景的核心测试点相同（忽略命名差异、表述方式差异）；\n   \n2. 输出要求：仅返回JSON数组，数组长度等于理论场景数，每个元素为0或1（1=匹配成功，0=匹配失败）；\n3. 输出格式：严格按以下顺序匹配（理论场景顺序不变），仅输出JSON，无任何多余内容。\n\n### 测试场景列表\n%s\n\n### 理论场景列表（按顺序匹配）\n%s\n\n### 输出示例（假设理论场景有3个，匹配结果为第1、3个成功）\n[1,0,1]",
                testScenarios, theoryScenarios);
    }

    /**
     * 解析AI返回的匹配结果（JSON数组）
     */
    private int parseAiMatchResult(String aiResponse, int theoryScenarioCount) throws JsonProcessingException {
        if (!StringUtils.hasText(aiResponse)) {
            logger.warn("AI匹配响应为空");
            return 0;
        }

        // 清理响应（去除可能的多余字符，如引号、空格）
        String cleanResponse = aiResponse.trim()
                .replaceAll("^\"|\"$", "") // 去除首尾引号
                .replaceAll("\\s+", ""); // 去除所有空格

        // 解析JSON数组
        List<Integer> matchResults = OBJECT_MAPPER.readValue(cleanResponse, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Integer.class));

        // 校验数组长度（避免AI返回格式错误）
        if (matchResults.size() != theoryScenarioCount) {
            logger.error("AI返回的匹配结果长度与理论场景数不一致，期望{}，实际{}", theoryScenarioCount, matchResults.size());
            return 0;
        }

        // 统计匹配成功的数量（求和）
        return matchResults.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 计算未覆盖的理论场景（基于AI匹配结果）
     */
    public List<String> getMissingScenariosByAI(List<String> testScenarios, List<String> theoryScenarios) {
        if (testScenarios.isEmpty() || theoryScenarios.isEmpty()) {
            return new ArrayList<>(theoryScenarios);
        }

        String prompt = buildMatchPrompt( theoryScenarios, testScenarios);
        try {
            String aiResponse = llmService.getMessage(prompt);
            List<Integer> matchResults = OBJECT_MAPPER.readValue(aiResponse.trim().replaceAll("\\s+", ""),
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Integer.class));

            // 收集匹配结果为0的理论场景（未覆盖）
            List<String> missingScenarios = new ArrayList<>();
            for (int i = 0; i < theoryScenarios.size(); i++) {
                if (matchResults.get(i) == 0) {
                    missingScenarios.add(theoryScenarios.get(i));
                }
            }
            return missingScenarios;
        } catch (Exception e) {
            logger.error("AI获取未覆盖场景失败，降级为手动规则", e);
            // 降级策略：使用手动规则
//            return theoryScenarios.stream()
//                    .filter(theory -> ScenarioMatchUtils.countMatchedScenarios(testScenarios, List.of(theory)) == 0)
//                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        return null;
    }
}