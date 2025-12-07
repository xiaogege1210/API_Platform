package com.example.demo.service.impl;

import com.example.demo.llm.LLMService;
import com.example.demo.service.TestCaseAnalysisService;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestCaseAnalysisServiceImpl implements TestCaseAnalysisService {
    @Autowired
    private LLMService llmService;
    // 封装为方法，统一处理Prompt构建
    /**
     * 构建标准化、高性能的测试脚本建议生成 Prompt
     * 核心优化：精简冗余描述、强化JSON格式约束、控制长度、统一转义规则
     */
    private String buildStandardPrompt(String api, String environment, String dependency, List<String> test) {
        // 1. 基础参数转义（避免破坏JSON结构，同时控制长度）
        String escapedApi = escapeAndTruncate(api, 5000);
        String escapedScenarios = escapeAndTruncate(environment, 1000);
        String escapedDep = escapeAndTruncate(dependency, 1000);
        String testscrpits="";
        for (String testname : test) {
            testscrpits+=escapeAndTruncate(testname, 5000);
        }
//        String escapedTest = escapeAndTruncate(test, 3000);
//        String escapedResult = escapeAndTruncate(testResult, 3000);

        // 2. 构建Prompt（结构化、无冗余、强约束）
        StringBuilder promptBuilder = new StringBuilder(8000); // 预设容量，减少扩容
        promptBuilder.append("### 角色\n")
                .append("你是自动化测试专家，需基于【给定测试场景】+API文档+接口依赖，分析测试脚本并输出JSON格式优化建议，无任何多余内容。\n\n")
// 输入数据（移除测试环境，明确剩余数据用途）
                .append("### 输入数据\n")
                .append("1. 给定测试场景：本次需覆盖的核心测试场景（如接口正向调用、异常入参、依赖失败等）\n")
                .append("   内容：").append(escapedScenarios).append("\n")
                .append("2. API文档：接口入参/出参规范、请求方式、异常码、边界值要求\n")
                .append("   内容：").append(escapedApi).append("\n")
                .append("3. 接口依赖：上下游接口调用顺序、参数传递规则、依赖数据生成逻辑\n")
                .append("   内容：").append(escapedDep).append("\n")
                .append("4. 测试脚本：待分析的自动化测试代码（含调用逻辑、参数、断言）\n")
                .append("   内容：").append(testscrpits).append("\n")
//                .append("5. 执行结果：脚本执行日志、失败报错、返回值、执行状态\n")
//                .append("   内容：").append(escapedResult).append("\n\n")
// 核心分析任务（移除环境相关，聚焦场景/脚本/失败）
                .append("### 核心分析任务\n")
                .append("1. 场景覆盖度分析（对照给定测试场景）：\n")
                .append("   - 识别给定场景中未被脚本覆盖的子场景（明确具体哪个场景未覆盖）\n")
                .append("   - 识别脚本中超出给定场景但无必要的冗余场景\n")
                .append("   - 结合API文档/接口依赖，分析未覆盖场景的原因（如脚本逻辑缺失、依赖未考虑）\n")
                .append("2. 脚本正确性分析（匹配给定测试场景）：\n")
                .append("   - 校验脚本语法/参数/依赖传递是否符合API规范，能否实现给定场景逻辑\n")
                .append("   - 校验脚本针对给定场景的断言是否完整（覆盖场景核心验证点）\n")
                .append("   - 校验脚本接口依赖调用顺序/参数传递是否正确，满足场景执行条件\n")
                .append("3. 执行失败分析（仅针对给定场景的失败脚本）：\n")
                .append("   - 定位给定场景下脚本执行失败的根因（如场景逻辑错误、断言不匹配、依赖失败）\n")
                .append("   - 给出针对该场景的具体修改方案（含可执行代码）\n")
                .append("   - 验证修改后是否能覆盖并正确执行该场景\n\n")
// 输出要求（字段关联给定场景，格式约束不变）
                .append("### 输出要求\n")
                .append("1. 优先级：high（必须改）/medium（建议优化）/low（体验提升）\n")
                .append("2. 建议字段（每个建议需明确关联给定测试场景）：\n")
                .append("   - id：S+3位数字（如S001），按顺序递增\n")
                .append("   - priority：严格匹配上述优先级\n")
                .append("   - dimension：仅允许场景覆盖/脚本正确性/执行失败\n")
                .append("   - problem_desc：描述问题（≤60字），需明确关联给定场景的具体内容\n")
                .append("   - modify_operation：具体修改动作（≤40字），针对给定场景优化\n")
                .append("   - modify_example：核心修改代码片段（≤150字），适配给定场景\n")
                .append("   - value：修改价值（≤30字），需明确覆盖/优化给定场景的效果\n")
                .append("3. 汇总字段：\n")
                .append("   - total_suggestions：建议总数（整数）\n")
                .append("   - high_priority_count：高优先级建议数（整数）\n")
                .append("   - core_problem：核心问题总结（≤100字），需聚焦给定场景的核心问题\n")
                .append("4. 格式约束：仅输出单行JSON字符串，无缩进、无换行、无注释，空值填空字符串\n")
                .append("5. 输出示例：\n")
                .append("{\"suggestions\":[{\"id\":\"S001\",\"priority\":\"high\",\"dimension\":\"场景覆盖\",\"problem_desc\":\"给定场景中'依赖接口失败'子场景未覆盖\",\"modify_operation\":\"新增依赖接口失败的异常测试用例\",\"modify_example\":\"def test_dep_fail():\\n    mock_dep_api(return_code=500)\\n    res = target_api.call()\\n    assert res['code'] == 503\",\"value\":\"覆盖依赖失败场景，提升测试完整性\"},{\"id\":\"S002\",\"priority\":\"high\",\"dimension\":\"执行失败\",\"problem_desc\":\"给定场景下依赖参数传递错误导致脚本失败\",\"modify_operation\":\"修正依赖参数名\",\"modify_example\":\"dep_param = dep_response['user_id'] # 原参数为userID\",\"value\":\"修复脚本执行失败问题\"}],\"summary\":{\"total_suggestions\":2,\"high_priority_count\":2,\"core_problem\":\"给定测试场景中依赖失败子场景未覆盖，且依赖参数传递错误导致脚本执行失败\"}}\n\n")
// 禁止项（强化约束，避免偏离）
                .append("### 禁止项\n")
                .append("- 禁止输出JSON以外的任何内容（包括说明、注释、缩进、换行）\n")
                .append("- 禁止提及测试环境相关内容，所有分析仅基于给定场景/API文档/接口依赖/脚本/执行结果\n")
                .append("- 禁止遗漏执行结果中的失败节点、未覆盖场景、断言缺失问题\n")
                .append("- 禁止modify_example给出无效代码，需保证语法正确、贴合给定场景\n")
                .append("- 禁止夸大优化价值，所有建议需基于输入数据客观分析");

        // 最终长度校验（避免超出大模型上下文限制）
;
        return promptBuilder.toString();
    }

    /**
     * 转义特殊字符 + 长度截断（避免参数过大导致400）
     * @param content 原始内容
     * @param maxLen 最大长度
     * @return 转义并截断后的内容
     */
    private String escapeAndTruncate(String content, int maxLen) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 转义核心特殊字符（避免破坏JSON结构）
        String escaped = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ") // 换行符替换为空格，减少长度
                .replace("\r", "")
                .replace("\t", " ");
        // 长度截断（保留末尾标识）
        if (escaped.length() > maxLen) {
            return escaped.substring(0, maxLen - 3) + "...";
        }
        return escaped;
    }
    @Override
    public String generateAnalysisAndSuggestions(String api, String environment, String dependency, List<String> test) throws JsonProcessingException {

        String response = llmService.getMessage(buildStandardPrompt(api,environment,dependency,test));
        return response;

    }
}