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
            return "输出错误";
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
                        "你是Java代码修复助手，负责诊断测试代码问题并给出正确解决方案。\n\n" +

                        "### 任务\n" +
                        "分析测试代码和错误信息，判断问题类型并给出相应处理：\n" +
                        "1. 如果是代码语法/编译错误 → 修复代码\n" +
                        "2. 如果是运行时异常 → 修复代码\n" +
                        "3. 如果是业务/数据问题 → 返回建议\n" +
                        "4. 如果是环境/配置问题 → 返回建议\n\n" +

                        "### 输入数据\n" +
                        "1. 测试代码：\n" + escapedTest + "\n" +
                        "2. 执行结果：\n" + escapedResult + "\n\n" +

                        "### 问题分类标准\n" +
                        "【需要修复代码的情况】\n" +
                        "✓ 编译错误：语法错误、类型不匹配、缺少导入语句\n" +
                        "✓ 运行时异常：NullPointerException、ClassCastException、AssertionError(断言语法错误)\n" +
                        "✓ 类型安全问题：Object与基本类型直接比较、未判空就类型转换\n" +
                        "✓ 代码逻辑错误：明显的逻辑缺陷导致的测试失败\n\n" +

                        "【需要返回建议的情况】\n" +
                        "✓ 业务错误：接口返回非200状态码（400/401/403/404/500等）\n" +
                        "✓ 数据问题：参数错误、ID不存在、数据过期、权限不足\n" +
                        "✓ 环境问题：网络超时、服务不可用、配置缺失\n" +
                        "✓ 预期不匹配：实际返回值与预期不同但代码逻辑正确\n" +
                        "✓ 断言失败但代码正确：测试数据失效导致的断言失败\n\n" +

                        "### 关键判断点\n" +
                        "1. 如果错误信息包含以下关键词之一，返回建议：\n" +
                        "   - HTTP状态码: 400, 401, 403, 404, 500\n" +
                        "   - 业务错误描述: \"token无效\", \"权限不足\", \"不存在\", \"参数错误\"\n" +
                        "   - 数据问题: \"id not exist\", \"invalid parameter\"\n" +
                        "2. 如果错误是代码语法或类型问题，修复代码\n" +
                        "3. 如果是NullPointerException等运行时异常，修复代码\n\n" +

                        "### 输出规则\n" +
                        "【情况一：需要修复代码】\n" +
                        "- 输出完整的修复后Java代码\n" +
                        "- 不要任何解释、注释、额外文字\n" +
                        "- 保持原有包名、类名、方法名\n" +
                        "- 修复后代码必须可编译运行\n\n" +

                        "【情况二：需要返回建议】\n" +
                        "- 输出格式：`// 建议：<简要问题描述>`\n" +
                        "- 描述基于错误信息，指出具体业务问题\n" +
                        "- 示例：`// 建议：接口返回400错误，chat_id不存在，请检查测试数据`\n" +
                        "- 只输出一行建议，不要多行\n\n" +

                        "### 修复原则（仅当修复代码时适用）\n" +
                        "1. 保持原功能不变\n" +
                        "2. 优先使用类型安全方法：response.jsonPath().getInt()等\n" +
                        "3. 添加必要的空值检查\n" +
                        "4. 不添加新功能、新方法\n" +
                        "5. 不改变测试断言的核心逻辑\n\n" +

                        "### 示例\n" +
                        "【错误信息包含400状态码】\n" +
                        "输入：HTTP状态码应为200 ==> expected: <200> but was: <400>\n" +
                        "输出：// 建议：接口返回400错误，参数无效，请检查请求参数\n\n" +

                        "【错误信息包含编译错误】\n" +
                        "输入：cannot find symbol\n" +
                        "输出：修复后的完整代码\n\n" +

                        "请根据以上规则处理："
        );
        return sb.toString();
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

