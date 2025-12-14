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
    public String generateAnalysisAndSuggestions(String api, String test) throws JsonProcessingException {
        // 1. 构建 Prompt
        String prompt = buildStandardPrompt(api,test);

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
    public String generateAnalysisAndSuggestionswiths(String api, String test) {
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
        System.out.println(testresult);



        StringBuilder sb = new StringBuilder();

        sb.append("### 角色\n" +
                "你是Java代码修复助手，专注于JUnit/TestNG+RestAssured测试代码的问题诊断与修复，能精准区分各类错误类型并给出对应解决方案。\n\n" +

                "### 任务\n" +
                "分析测试代码和错误信息，按**错误类型分级处理**：\n" +
                "1. 编译类错误（语法/依赖/类型/导入/方法签名等）→ 修复代码\n" +
                "2. 运行时异常（空指针/类型转换/数组越界等）→ 修复代码\n" +
                "3. 业务/数据类问题（接口状态码异常/数据无效等）→ 返回建议\n" +
                "4. 环境/配置类问题（超时/连接失败/配置缺失等）→ 返回建议\n" +
                "5. 时间超限问题（接口超时/测试框架超时）→ 修复代码（添加重试+延长超时）\n" +
                "6. 断言失败（代码语法正确但结果不匹配）→ 严格按场景处理：\n" +
                "   - 断言语法错误（如类型不匹配、方法调用错误）→ 修复代码\n" +
                "   - 接口响应值与预期不符（如HTTP状态码200预期但实际400）→ 返回建议\n\n" +

                "### 输入数据\n" +
                "1. 测试代码：{escapedTest}\n" +
                "2. 执行结果：{escapedResult}\n\n" +

                "### 错误分类标准（强制遵循）\n" +
                "【必须修复代码的错误类型】\n" +
                "✓ 编译错误（语法层面）：\n" +
                "  - 关键词：'错误:'、'cannot find symbol'、'; expected'、'expected'、'illegal start'、'not a statement'、'missing return statement'、'method does not override'、'.class expected'\n" +
                "  - 场景：缺少分号/括号、变量未定义、方法签名错误、导入缺失、返回值缺失\n" +
                "✓ 类型相关错误：\n" +
                "  - 关键词：'不兼容的类型'、'incompatible types'、'类型不匹配'、'type mismatch'、'bad operand types'、'操作数类型错误'\n" +
                "  - 场景：Object与基本类型直接比较、错误的类型转换、返回值类型不匹配\n" +
                "✓ 运行时异常：\n" +
                "  - 关键词：'NullPointerException'、'ClassCastException'、'IndexOutOfBoundsException'、'ArrayIndexOutOfBoundsException'\n" +
                "  - 场景：空指针调用方法、错误的类型强转、数组越界访问\n" +
                "✓ 未处理的异常：\n" +
                "  - 场景：方法抛出检查型异常但未声明throws\n" +
                "✓ 时间超限问题：\n" +
                "  - 关键词：'timeout'、'SocketTimeoutException'、'ThreadTimeoutException'、'连接超时'\n" +
                "  - 场景：接口请求超时、测试用例执行超时\n" +
                "✓ 断言语法错误：\n" +
                "  - 场景：断言中使用错误的方法/类型（如用assertEquals比较Object和int）、断言方法参数数量错误\n\n" +

                "【必须返回建议的错误类型】\n" +
                "✓ 业务错误（重点强化断言失败场景）：\n" +
                "  - 关键词：HTTP状态码（400/401/403/404/500）、'HTTP状态码应为200 ==> expected: <200> but was: <400>'、'token无效'、'权限不足'、'不存在'、'参数错误'、'invalid'、'unauthorized'、'forbidden'、'not found'\n" +
                "  - 场景：接口返回业务错误码、权限校验失败、资源不存在、请求参数无效导致断言失败\n" +
                "✓ 数据问题：\n" +
                "  - 关键词：'id not exist'、'invalid parameter'、'data not found'、'数据过期'、'用户名已存在'\n" +
                "  - 场景：测试数据无效、参数格式错误、数据冲突导致断言失败\n" +
                "✓ 环境配置问题：\n" +
                "  - 关键词：'connection'、'服务不可用'、'网络'、'UnknownHostException'、'配置缺失'、'baseURL错误'\n" +
                "  - 场景：服务连接失败、域名解析错误、环境变量未配置\n" +
                "✓ 正常的断言失败（核心强化）：\n" +
                "  - 关键词：'AssertionFailedError'、'expected: <...> but was: <...>'、'HTTP状态码应为XXX但实际为XXX'\n" +
                "  - 场景：代码逻辑正确，但接口返回值/测试数据与断言预期值不符（如预期200实际400）\n" +
                "✓ 服务端问题：\n" +
                "  - 关键词：'500 Internal Server Error'、'服务端异常'、'NullPointerException'（服务端堆栈）\n" +
                "  - 场景：接口内部报错、服务未启动\n" +
                "✓ 依赖缺失：\n" +
                "  - 关键词：'ClassNotFoundException'\n" +
                "  - 场景：缺少第三方类依赖\n\n" +

                "### 关键判断规则（优先级从高到低）\n" +
                "1. 若错误信息同时包含多种错误类型，优先按「必须修复代码」处理（如编译错误+业务错误，先修复编译错误）\n" +
                "2. 编译失败（退出码：1）且含行号+错误描述，直接判定为编译错误，必须修复\n" +
                "3. 时间超限问题需主动添加重试机制和超时配置，属于代码修复范畴\n" +
                "4. 断言失败核心判断：\n" +
                "   - 若断言失败是因**语法/类型问题**（如assertEquals传参类型不匹配）→ 修复代码\n" +
                "   - 若断言失败是因**接口返回值与预期不符**（如预期200实际400的AssertionFailedError）→ 返回建议\n" +
                "5. 类型安全问题（如用get()代替getInt()）属于代码错误，必须修复\n" +
                "6. 只要错误信息包含「AssertionFailedError」+「expected: <...> but was: <...>」且无语法错误，直接返回建议\n\n" +

                "### 输出规则\n" +
                "【情况一：需要修复代码】\n" +
                "- 输出要求：仅完整的修复后Java代码，无任何解释、注释、额外文字\n" +
                "- 格式约束：保持原包名、类名、方法名，修复后代码可直接编译运行\n" +
                "- 特殊修复要求：\n" +
                "  - 时间超限：添加TestNG/JUnit重试机制+延长RestAssured超时时间（默认重试3次，超时10秒）\n" +
                "  - 类型不匹配：使用RestAssured类型安全方法（getInt()/getString()/getBoolean()）\n" +
                "  - 空指针：添加必要的空值检查\n" +
                "  - 保持原测试逻辑和断言核心不变\n\n" +

                "【情况二：需要返回建议】\n" +
                "- 输出格式：`// 建议：<具体问题描述>`\n" +
                "- 要求：单行输出，基于错误信息精准定位问题（含具体测试方法名、状态码/数值差异）\n" +
                "- 核心示例：\n" +
                "  - 接口状态码断言失败：`// 建议：测试方法testGetChatMembersWithDefaultParameters执行失败，接口返回400状态码（预期200），请检查请求参数或测试数据有效性`\n" +
                "  - 字段值断言失败：`// 建议：测试方法testXXX执行失败，字段xxx实际值为YYY（预期ZZZ），请核对接口文档或测试数据`\n" +
                "- 只输出一行建议，不要多行\n\n" +

                "### 修复原则（仅修复代码时适用）\n" +
                "1. 最小修改原则：仅修复问题点，不改动无关代码\n" +
                "2. 类型安全优先：优先使用RestAssured提供的类型安全JSON路径方法\n" +
                "3. 超时修复标准：\n" +
                "   - TestNG：为测试方法添加@RetryAnalyzer(RetryAnalyzer.class)注解（默认重试3次）\n" +
                "   - JUnit：使用@Retryable注解实现重试\n" +
                "   - RestAssured配置：setConnectionTimeout(10000)、setResponseTimeout(10000)\n" +
                "4. 空值处理：对可能为null的对象添加非空判断，避免NPE\n" +
                "5. 不新增功能、不修改原有业务逻辑、不删除有效代码\n" +
                "6. 编译错误修复后，必须保证代码可通过javac编译\n\n" +

                "### 重点示例（强化断言失败场景）\n" +
                "【示例1：HTTP状态码断言失败】\n" +
                "输入错误：\n" +
                "testGetChatMembersWithDefaultParameters：异常类型：AssertionFailedError\n" +
                "异常消息：HTTP状态码应为200 ==> expected: <200> but was: <400>\n" +
                "输出：// 建议：测试方法testGetChatMembersWithDefaultParameters执行失败，接口返回400状态码（预期200），请检查请求参数或测试数据有效性\n\n" +

                "【示例2：字段值断言失败】\n" +
                "输入错误：\n" +
                "testGetUserInfo：异常类型：AssertionFailedError\n" +
                "异常消息：expected: <admin> but was: <guest>\n" +
                "输出：// 建议：测试方法testGetUserInfo执行失败，用户角色实际值为guest（预期admin），请核对测试账号权限或接口返回值\n\n" +

                "【示例3：断言语法错误（类型不匹配）】\n" +
                "输入错误：\n" +
                "test.java:15: 错误: 不兼容的类型: Object无法转换为int\n" +
                "Assert.assertEquals(response.jsonPath().get(\"code\"), 200);\n" +
                "输出：修复后的完整Java代码（将get()改为getInt()）\n\n" +

                "请严格按照以上规则执行，确保断言失败场景的判断精准、建议输出贴合实际错误信息。");
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
    private String buildStandardPrompt(String api, String test) {
        String escapedApi = escapeAndTruncate(api, 5000);

        String escapedTest = escapeAndTruncate(test, 30000);


        StringBuilder sb = new StringBuilder(200000);

        sb.append(
                "### 角色\n" +
                        "你是资深自动化测试专家，具备丰富的API测试经验。需要基于完整的测试上下文（场景+API文档+依赖关系+执行结果），对测试脚本进行深度结构化分析，输出专业、可执行的优化建议。\n\n" +

                        "### 核心分析能力要求\n" +
                        "1. **深度代码审查**：分析测试脚本的代码质量、设计模式、可维护性\n" +
                        "2. **场景适配性分析**：验证测试脚本是否完全覆盖给定测试场景，识别遗漏和冗余\n" +
                        "3. **API合规性检查**：确保脚本遵循API文档规范（参数、鉴权、数据格式）\n" +
                        "4. **依赖合理性验证**：检查接口调用顺序、数据传递、状态管理的正确性\n" +
                        "5. **失败根因定位**：基于执行结果精准定位问题，提供可执行的修复方案\n\n" +

                        "【API文档】" + escapedApi + "\n" +
                        "  - 用途：验证脚本参数、请求方式、响应处理的正确性\n" +
                        "  - 重点：接口路径、HTTP方法、请求头、请求体结构、响应码定义\n\n" +

                        "【测试脚本】" + escapedTest + "\n" +
                        "  - 用途：分析代码实现质量、测试设计合理性\n" +
                        "  - 重点：测试方法组织、断言策略、数据驱动、异常处理\n\n" +



                        "### 核心分析维度（按优先级排序）\n" +
                        "#### 1. 场景覆盖度分析（权重40%）\n" +
                        "- 覆盖完整性：是否覆盖所有给定的测试场景\n" +
                        "- 场景缺失：识别未覆盖的关键业务场景及其风险等级\n" +
                        "- 场景冗余：识别重复或无效的测试场景\n" +
                        "- 边界覆盖：边界条件、异常场景的覆盖情况\n" +
                        "- 业务流验证：端到端业务流程是否完整验证\n\n" +

                        "#### 2. 脚本正确性分析（权重30%）\n" +
                        "- 代码规范性：命名规范、代码结构、注释质量\n" +
                        "- 参数正确性：必填参数、参数格式、参数组合\n" +
                        "- 鉴权机制：Token管理、权限验证、安全配置\n" +
                        "- 依赖调用：接口调用顺序、数据依赖关系\n" +
                        "- 断言完整性：响应码、数据结构、业务规则断言\n" +
                        "- 异常处理：超时、网络异常、业务异常的处理逻辑\n" +
                        "- 数据管理：测试数据生成、清理、隔离机制\n\n" +

                        "#### 3. 执行失败根因分析（权重30%）\n" +
                        "- 失败分类：编译错误、运行时异常、断言失败、环境问题\n" +
                        "- 根本原因定位：代码缺陷、数据问题、环境配置、依赖服务\n" +
                        "- 修复优先级：按影响范围和修复成本确定优先级\n" +
                        "- 修复方案：提供可直接执行的代码修改或配置调整\n\n" +

                        "### 输出要求（STRICT JSON FORMAT）\n" +
                        "#### 必须输出合法的JSON对象，不允许任何JSON之外的文本\n" +
                        "#### JSON结构定义：\n" +
                        "{\n" +
                        "  \"metrics\": {\n" +
                        "    \"totalLines\": 整数,               // 脚本总行数\n" +
                        "    \"testMethodCount\": 整数,          // 测试方法总数\n" +
                        "    \"assertionCount\": 整数,           // 断言总数\n" +
                        "    \"coverageScore\": 整数,            // 场景覆盖度评分(0-100)\n" +
                        "    \"codeQualityScore\": 整数,         // 代码质量评分(0-100)\n" +
                        "    \"executionStabilityScore\": 整数,  // 执行稳定性评分(0-100)\n" +
                        "    \"missingScenariosCount\": 整数,    // 未覆盖的场景数量\n" +
                        "    \"redundantScenariosCount\": 整数,  // 冗余的场景数量\n" +
                        "    \"criticalIssuesCount\": 整数       // 关键问题数量\n" +
                        "  },\n" +
                        "  \"suggestions\": [\n" +
                        "    {\n" +
                        "      \"id\": \"S001\",                  // 建议唯一标识，递增编号\n" +
                        "      \"priority\": \"high\",           // high/medium/low 三选一\n" +
                        "      \"dimension\": \"场景覆盖\",       // 场景覆盖/脚本正确性/执行失败\n" +
                        "      \"category\": \"边界条件缺失\",     // 具体问题分类\n" +
                        "      \"problem_desc\": \"≤40字问题描述，需具体到代码行或场景\",\n" +
                        "      \"root_cause\": \"≤50字根本原因分析，包含技术细节\",\n" +
                        "      \"modify_operation\": \"≤30字具体修改操作\",\n" +
                        "      \"modify_example\": \"≤100字代码示例或配置修改\",\n" +
                        "      \"expected_effect\": \"≤20字预期效果\",\n" +
                        "      \"related_scenario\": \"相关场景编号或名称\",\n" +
                        "      \"risk_level\": \"high\"         // high/medium/low 三选一\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"summary\": {\n" +
                        "    \"overall_assessment\": \"≤100字整体评估，突出核心优缺点\",\n" +
                        "    \"key_strengths\": [\"优势1\", \"优势2\"],  // 最多3项\n" +
                        "    \"key_improvements\": [\"待改进1\", \"待改进2\"],  // 最多3项\n" +
                        "    \"immediate_actions\": [\"立即执行1\", \"立即执行2\"]  // 最高优先级的修复项\n" +
                        "  }\n" +
                        "}\n\n" +

                        "### 字段填写规范\n" +
                        "1. **metrics字段**：基于代码和执行结果客观计算，无结果则填0\n" +
                        "2. **suggestions数组**：至少输出3条建议，最多10条\n" +
                        "3. **priority优先级**：\n" +
                        "   - high：影响功能正确性或测试执行的关键问题\n" +
                        "   - medium：代码质量、可维护性改进\n" +
                        "   - low：优化建议、非关键改进\n" +
                        "4. **category分类**：边界条件缺失、参数错误、断言不足、依赖错误、异常处理缺失、代码冗余、性能问题等\n" +
                        "5. **risk_level风险等级**：\n" +
                        "   - high：可能导致测试误报/漏报、生产环境问题\n" +
                        "   - medium：影响测试稳定性、可维护性\n" +
                        "   - low：代码风格、轻微优化\n\n" +

                        "### 分析步骤（供思考参考，不输出）\n" +
                        "1. 扫描测试脚本，提取测试方法、断言、参数等关键元素\n" +
                        "2. 将测试场景逐一映射到脚本实现，标识覆盖状态\n" +
                        "3. 对照API文档，验证请求构造和响应处理的正确性\n" +
                        "4. 分析依赖关系，验证数据传递和调用顺序\n" +
                        "5. 解析执行结果，定位失败原因并归类\n" +
                        "6. 综合评估测试脚本的完整性、正确性和健壮性\n" +
                        "7. 生成结构化的优化建议，按优先级排序\n\n" +

                        "### 严格输出规则\n" +
                        "1. **必须输出合法JSON**：以 { 开头，以 } 结束，符合JSON语法\n" +
                        "2. **不输出任何非JSON内容**：包括解释、说明、markdown格式等\n" +
                        "3. **字段长度限制**：严格遵守各字段字数限制\n" +
                        "4. **建议数量**：优先保证质量，不强制填满10条\n" +
                        "5. **基于证据**：所有建议必须有输入数据支持\n\n" +

                        "### 评分标准参考\n" +
                        "1. **覆盖度评分**：覆盖场景数/总场景数 * 100\n" +
                        "2. **代码质量评分**：基于代码规范、复用性、可读性综合评估\n" +
                        "3. **执行稳定性评分**：基于执行结果的成功率、稳定性评估\n" +
                        "4. **关键问题**：直接导致测试失败或结果不可信的问题\n\n" +

                        "现在开始分析，并输出完整的JSON结果：\n"
        );

        return sb.toString();
    }


}

