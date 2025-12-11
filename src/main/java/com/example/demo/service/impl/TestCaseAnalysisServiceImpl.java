package com.example.demo.service.impl;

import com.example.demo.dto.TestCaseResultDto;
import com.example.demo.llm.LLMService;
import com.example.demo.service.TestCaseAnalysisService;
import com.example.demo.utils.LLMJsonValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//todo:模型生成完整不够，总是生成部分代码或者运行建议，
@Service
public class TestCaseAnalysisServiceImpl implements TestCaseAnalysisService {

    @Autowired
    private LLMService llmService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * 针对多个接口文档自动生成建议，无需传入环境和数据
     * @param api 多个接口文档内容
     * @param test 测试脚本内容
     * @return 分析建议的JSON字符串
     */
    @Override
    public String generateAnalysisAndSuggestions(String api, String test) {
        // 1. 构建针对多接口分析的Prompt
        String prompt = buildMultiApiAnalysisPrompt(api, test);

        try {
            // 2. 调用 LLM
            String llmRawOutput = llmService.getMessage(prompt);

            System.out.println("多接口分析 LLM 原始输出：");
            System.out.println(llmRawOutput);

            // 3. 使用 JSON 校验与修复工具
            String safeJson = LLMJsonValidator.validateAndFixJson(
                    llmRawOutput,
                    buildDefaultMultiApiAnalysisJson()
            );

            System.out.println("校验后的安全 JSON：");
            System.out.println(safeJson);

            return safeJson;

        } catch (Exception e) {
            System.err.println("多接口分析失败: " + e.getMessage());
            return buildDefaultMultiApiAnalysisJson();
        }
    }

    /**
     * 根据脚本以及脚本执行结果自动优化脚本
     * @param test 测试脚本
     * @param testResult 测试执行结果
     * @return 优化后的脚本（或包含优化信息的JSON）
     * @throws JsonProcessingException
     */
    @Override
    public String OptimizedScript(String test, TestCaseResultDto testResult) throws JsonProcessingException {
        // 1. 构建脚本优化Prompt
        String prompt = buildScriptOptimizationPrompt(test, testResult);

        try {
            // 2. 调用 LLM
            String llmRawOutput = llmService.getMessage(prompt);

            System.out.println("脚本优化 LLM 原始输出：");
            System.out.println(llmRawOutput);

            // 3. 解析并优化脚本
            return llmRawOutput;

        } catch (Exception e) {
            System.err.println("脚本优化失败: " + e.getMessage());
            return buildDefaultOptimizedScript(test);
        }
    }

    // =====================================
    // 私有辅助方法
    // =====================================

    /**
     * 构建多接口分析的Prompt
     */
    private String buildMultiApiAnalysisPrompt(String api, String test) {
        String escapedApi = escapeAndTruncate(api, 8000); // 多接口可能较长
        String escapedTest = escapeAndTruncate(test, 30000);

        StringBuilder sb = new StringBuilder();

        sb.append(
                "### 角色\n" +
                        "你是自动化测试专家，需要基于【多个API接口文档】和测试脚本，分析测试覆盖度和给出优化建议。\n" +
                        "注意：这里包含多个接口，你需要考虑接口间的依赖关系和测试完整性。\n\n" +

                        "### 输入数据\n" +
                        "1. API接口文档（可能包含多个接口）：\n" + escapedApi + "\n" +
                        "2. 测试脚本：\n" + escapedTest + "\n\n" +

                        "### 分析任务\n" +
                        "1. **接口覆盖分析**：统计脚本覆盖了哪些接口，哪些接口未被覆盖\n" +
                        "2. **场景完整性分析**：针对已覆盖的接口，分析是否覆盖了主要业务场景（增删改查、边界条件等）\n" +
                        "3. **跨接口依赖测试**：检查接口间的依赖关系是否得到充分测试\n" +
                        "4. **测试效率分析**：评估是否有重复测试或可以合并的测试场景\n\n" +

                        "### 输出要求\n" +
                        "- 必须输出 **合法 JSON**\n" +
                        "- JSON 必须以 { 开头，以 } 结束\n" +
                        "- 重点突出多接口测试的特点\n\n" +

                        "{\n" +
                        "  \"coverage_analysis\": {\n" +
                        "    \"total_apis\": 整数, // 从文档中提取的API总数\n" +
                        "    \"covered_apis\": 整数, // 测试覆盖的API数量\n" +
                        "    \"coverage_rate\": 0-100 的整数, // 覆盖率\n" +
                        "    \"uncovered_apis\": [ // 未覆盖的API列表\n" +
                        "      {\n" +
                        "        \"api_name\": \"接口名称\",\n" +
                        "        \"method\": \"GET/POST/PUT/DELETE\",\n" +
                        "        \"path\": \"接口路径\",\n" +
                        "        \"suggested_test\": \"建议的测试场景\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"scenario_completeness\": {\n" +
                        "    \"critical_scenarios_covered\": true/false, // 是否覆盖关键场景\n" +
                        "    \"boundary_cases_tested\": true/false, // 是否测试边界条件\n" +
                        "    \"error_cases_tested\": true/false // 是否测试异常情况\n" +
                        "  },\n" +
                        "  \"cross_api_testing\": {\n" +
                        "    \"dependency_tested\": true/false, // 是否测试接口依赖\n" +
                        "    \"workflow_coverage\": \"高/中/低\", // 业务流程覆盖度\n" +
                        "    \"suggested_workflows\": [ // 建议测试的业务流程\n" +
                        "      \"业务流程描述\"\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"optimization_suggestions\": [\n" +
                        "    {\n" +
                        "      \"type\": \"coverage/duplication/optimization\",\n" +
                        "      \"description\": \"建议描述\",\n" +
                        "      \"priority\": \"high/medium/low\",\n" +
                        "      \"implementation\": \"实现建议\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n"
        );

        return sb.toString();
    }

    /**
     * 构建脚本优化的Prompt（修改版：要求只输出优化后的代码）
     */
    private String buildScriptOptimizationPrompt(String test, TestCaseResultDto testResult) {
        String escapedTest = escapeAndTruncate(test, 30000);
        String testresult=testResult.toString();
        if(testresult!=null){
            testresult="执行时间："+testResult.getExecuteTime()+"输出结果："+testResult.getOutputText()+"失败原因："+testResult.getFailureReason();
        }
        String escapedResult = escapeAndTruncate(escapedTest, 8000);



        StringBuilder sb = new StringBuilder();

        sb.append(
                "### 角色\n" +
                        "你是Java自动化测试脚本优化专家，专门优化Selenium/JUnit/TestNG等框架的测试代码。务必输出完整的代码\n\n" +

                        "### 输入数据\n" +
                        "1. 原始测试脚本：\n" + escapedTest + "\n" +
                        "2. 测试执行结果：\n" + escapedResult + "\n\n" +

                        "### 优化任务\n" +
                        "请直接优化以下Java测试脚本，只返回优化后的完整代码，不要输出任何JSON格式、解释或额外说明。\n\n" +

                        "### 优化重点（按优先级）\n" +
                        "1. 【关键】修复测试失败问题（如果测试结果中有失败信息）\n" +
                        "2. 性能优化：替换Thread.sleep为显式等待，优化定位器性能\n" +
                        "3. 稳定性优化：添加重试机制，使用更稳定的等待策略\n" +
                        "4. 可读性优化：改进命名，添加必要注释，简化复杂逻辑\n" +
                        "5. 可维护性优化：提取公共方法，消除重复代码，改进代码结构\n\n" +

                        "### 具体优化规则\n" +
                        "1. 将Thread.sleep(毫秒)替换为WebDriverWait和ExpectedConditions\n" +
                        "2. 将不稳定的XPath定位器优化为CSS选择器或ID定位器\n" +
                        "3. 为关键操作添加try-catch异常处理\n" +
                        "4. 提取硬编码的URL、用户名、密码为常量\n" +
                        "5. 拆分过长的方法（>30行）\n" +
                        "6. 增强断言信息，使用assertThat等更丰富的断言方法\n" +
                        "7. 添加必要的JavaDoc注释\n" +
                        "8. 确保代码可以直接编译运行\n\n" +

                        "### 输出要求（非常重要！）\n" +
                        "- 只输出优化后的完整Java代码\n" +
                        "- 不要输出任何解释、分析或JSON格式\n" +
                        "- 不要输出```java```这样的代码块标记\n" +
                        "- 确保代码可以直接复制粘贴使用\n\n" +
                        "现在请优化以下脚本：\n" +
                        "原始脚本：\n" + escapedTest + "\n\n" +
                        "优化后的代码："
        );

        return sb.toString();
    }


    /**
     * 处理脚本优化结果
     */
    private String processScriptOptimization(String llmRawOutput, String originalTest, TestCaseResultDto testResult) {
        // 1. 首先尝试JSON验证和修复
        String safeJson = LLMJsonValidator.validateAndFixJson(
                llmRawOutput,
                buildDefaultOptimizationJson(originalTest)
        );

        try {
            // 2. 解析JSON
            Map<String, Object> optimization = objectMapper.readValue(safeJson, Map.class);

            // 3. 如果需要，可以进一步处理优化后的脚本
            String optimizedScript = (String) ((Map<String, Object>) optimization.get("optimization_summary")).get("optimized_script");

            if (optimizedScript == null || optimizedScript.trim().isEmpty()) {
                // 如果没有返回优化后的脚本，尝试从其他地方提取或生成
                optimizedScript = extractScriptFromJson(optimization);
            }

            // 4. 如果还是为空，使用原始脚本
            if (optimizedScript == null || optimizedScript.trim().isEmpty()) {
                System.err.println("警告：LLM未返回优化后的脚本，使用原始脚本");
                optimizedScript = originalTest;
                // 更新JSON中的脚本
                ((Map<String, Object>) optimization.get("optimization_summary")).put("optimized_script", originalTest);
                ((Map<String, Object>) optimization.get("optimization_summary")).put("original_lines", countLines(originalTest));
                ((Map<String, Object>) optimization.get("optimization_summary")).put("optimized_lines", countLines(originalTest));
            }

            // 5. 重新序列化为JSON
            return objectMapper.writeValueAsString(optimization);

        } catch (Exception e) {
            System.err.println("解析优化结果失败: " + e.getMessage());
            return buildDefaultOptimizationJson(originalTest);
        }
    }

    /**
     * 从JSON中提取脚本
     */
    private String extractScriptFromJson(Map<String, Object> optimization) {
        // 尝试从不同字段提取脚本
        if (optimization.containsKey("optimized_script")) {
            Object script = optimization.get("optimized_script");
            if (script instanceof String) {
                return (String) script;
            }
        }

        // 尝试从modification_details中重构
        if (optimization.containsKey("modification_details")) {
            // 简化实现：这里应该根据修改细节重构脚本
            // 实际应用中需要更复杂的逻辑
            System.out.println("尝试从修改细节中重构脚本...");
        }

        return null;
    }

    /**
     * 构建默认的多接口分析JSON
     */
    private String buildDefaultMultiApiAnalysisJson() {
        return "{\n" +
                "  \"coverage_analysis\": {\n" +
                "    \"total_apis\": 0,\n" +
                "    \"covered_apis\": 0,\n" +
                "    \"coverage_rate\": 0,\n" +
                "    \"uncovered_apis\": []\n" +
                "  },\n" +
                "  \"scenario_completeness\": {\n" +
                "    \"critical_scenarios_covered\": false,\n" +
                "    \"boundary_cases_tested\": false,\n" +
                "    \"error_cases_tested\": false\n" +
                "  },\n" +
                "  \"cross_api_testing\": {\n" +
                "    \"dependency_tested\": false,\n" +
                "    \"workflow_coverage\": \"低\",\n" +
                "    \"suggested_workflows\": []\n" +
                "  },\n" +
                "  \"optimization_suggestions\": [\n" +
                "    {\n" +
                "      \"type\": \"coverage\",\n" +
                "      \"description\": \"无法解析API文档，请检查文档格式\",\n" +
                "      \"priority\": \"high\",\n" +
                "      \"implementation\": \"确保API文档为标准的OpenAPI/Swagger格式\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 构建默认的优化脚本JSON
     */
    private String buildDefaultOptimizationJson(String originalTest) {
        int lines = countLines(originalTest);

        return "{\n" +
                "  \"optimization_summary\": {\n" +
                "    \"original_lines\": " + lines + ",\n" +
                "    \"optimized_lines\": " + lines + ",\n" +
                "    \"complexity_reduction\": 0,\n" +
                "    \"estimated_performance_gain\": 0,\n" +
                "    \"optimized_script\": \"" + escapeJsonString(originalTest) + "\"\n" +
                "  },\n" +
                "  \"key_issues_fixed\": [\n" +
                "    {\n" +
                "      \"issue_type\": \"stability\",\n" +
                "      \"description\": \"LLM处理失败，使用原始脚本\",\n" +
                "      \"severity\": \"medium\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"modification_details\": [\n" +
                "    {\n" +
                "      \"line_numbers\": \"N/A\",\n" +
                "      \"original_code\": \"N/A\",\n" +
                "      \"optimized_code\": \"N/A\",\n" +
                "      \"reason\": \"LLM处理失败\",\n" +
                "      \"impact\": \"无修改\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"best_practices_applied\": [\n" +
                "    \"建议检查测试脚本格式，确保为有效的Java代码\"\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 构建默认优化脚本（字符串格式）
     */
    private String buildDefaultOptimizedScript(String originalTest) {
        return originalTest + "\n\n" +
                "// ===== 自动化优化建议 =====\n" +
                "// 由于LLM处理失败，以下是通用优化建议：\n" +
                "// 1. 确保使用显式等待而非Thread.sleep\n" +
                "// 2. 添加适当的异常处理\n" +
                "// 3. 提取重复代码为工具方法\n" +
                "// 4. 添加有意义的断言消息";
    }

    /**
     * 计算代码行数
     */
    private int countLines(String text) {
        if (text == null || text.isEmpty()) return 0;
        return text.split("\r\n|\r|\n").length;
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJsonString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 转义并截断字符串（原有方法保持不变）
     */
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

    /**
     * 构建标准Prompt（原有方法保持不变）
     */
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
}