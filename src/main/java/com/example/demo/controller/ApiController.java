package com.example.demo.controller;

import com.example.demo.dto.TestCaseResultDto;
import com.example.demo.model.ApiEndpoint;
import com.example.demo.model.GenerateCodeRequest;
import com.example.demo.service.ExternalTestCaseGeneratorService;
import com.example.demo.service.ParserService;
import com.example.demo.service.TestCaseAnalysisService;
import com.example.demo.service.TestCaseManagementService;
import com.example.demo.service.TestExecutionService;
import com.example.demo.service.impl.TestExecutionServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Controller
public class ApiController {

    @Autowired
    private ParserService parserService;

    @Autowired
    private TestExecutionServiceImpl testExecutionService;

    @Autowired
    private ExternalTestCaseGeneratorService externalTestCaseGeneratorService;

//    @Autowired
//    private TestCaseManagementService testCaseManagementService;

    @Autowired
    private TestCaseAnalysisService testCaseAnalysisService;

    public String index() { return "index"; }

    // 解析接口
    @PostMapping("/api/parse")
    @ResponseBody
    public List<ApiEndpoint> parse(@RequestParam("file") MultipartFile file) throws Exception {
        return parserService.parseSwaggerFile(file);
    }

    // 测试代码生成接口，调用外部服务生成测试代码
    @PostMapping("/api/generate")
    @ResponseBody
    public String generateCode(@RequestBody GenerateCodeRequest request) {
        // 调用外部服务生成测试用例
        return externalTestCaseGeneratorService.generateTestCase(request);
    }

    // 执行测试用例接口 - 直接执行传入的测试代码
    @PostMapping("/api/execute-direct")
    @ResponseBody
    public String executeTestsDirect(@RequestBody Map<String, String> request) {
        StringBuilder results = new StringBuilder();

        try {
            // 1. 获取直接传入的测试代码
            String testCode = request.get("testCode");
            if (testCode == null || testCode.isEmpty()) {
                return "❌ 错误：未提供测试代码！\n";
            }

            // 2. 输出生成的代码概览
            results.append("📋 测试代码概览：\n");
            int codeLines = testCode.split("\\n").length;
            int methodCount = 0;
            for (String line : testCode.split("\\n")) {
                if (line.trim().startsWith("@Test")) {
                    methodCount++;
                }
            }
            results.append("   - 总代码行数: " + codeLines + "\n");
            results.append("   - 测试方法数: " + methodCount + "\n\n");

            // 3. 直接执行传入的测试代码
            results.append("🚀 开始执行测试代码...\n\n");
            String executionResult = testExecutionService.executeGeneratedTests(testCode);
            results.append(executionResult);

        } catch (Exception e) {
            results.append("❌ 执行测试时发生错误: " + e.getMessage() + "\n");
            e.printStackTrace();
        }

        return results.toString();
    }

    // ==================== 测试用例管理接口 ====================

    /**
     * 保存测试用例
     */
    @PostMapping("/api/testcase/save")
    @ResponseBody
    public Map<String, Object> saveTestCase(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String testCaseId = request.get("testCaseId");
        String testCode = request.get("testCode");

        if (testCaseId == null || testCode == null) {
            result.put("success", false);
            result.put("message", "测试用例ID和测试代码不能为空");
            return result;
        }

        testCaseManagementService.saveTestCase(testCaseId, testCode);
        result.put("success", true);
        result.put("message", "测试用例保存成功");
        return result;
    }

    /**
     * 获取测试用例
     */
    @GetMapping("/api/testcase/get")
    @ResponseBody
    public Map<String, Object> getTestCase(@RequestParam("testCaseId") String testCaseId) {
        Map<String, Object> result = new HashMap<>();
        String testCode = testCaseManagementService.getTestCase(testCaseId);

        if (testCode == null) {
            result.put("success", false);
            result.put("message", "测试用例不存在");
            return result;
        }

        result.put("success", true);
        result.put("testCode", testCode);
        return result;
    }

    /**
     * 更新测试用例
     */
    @PostMapping("/api/testcase/update")
    @ResponseBody
    public Map<String, Object> updateTestCase(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String testCaseId = request.get("testCaseId");
        String testCode = request.get("testCode");

        if (testCaseId == null || testCode == null) {
            result.put("success", false);
            result.put("message", "测试用例ID和测试代码不能为空");
            return result;
        }

        boolean success = testCaseManagementService.updateTestCase(testCaseId, testCode);
        result.put("success", success);
        result.put("message", success ? "测试用例更新成功" : "测试用例不存在");
        return result;
    }

    /**
     * 删除测试用例
     */
    @DeleteMapping("/api/testcase/delete")
    @ResponseBody
    public Map<String, Object> deleteTestCase(@RequestParam("testCaseId") String testCaseId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = testCaseManagementService.deleteTestCase(testCaseId);
        result.put("success", success);
        result.put("message", success ? "测试用例删除成功" : "测试用例不存在");
        return result;
    }

    /**
     * 获取所有测试用例
     */
    @GetMapping("/api/testcase/all")
    @ResponseBody
    public Map<String, Object> getAllTestCases() {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> testCases = testCaseManagementService.getAllTestCases();
        result.put("success", true);
        result.put("testCases", testCases);
        return result;
    }

    // ==================== 测试用例分析接口 ====================

    /**
     * 生成测试用例的分析和建议(多个脚本）
     */
    @PostMapping("/api/testcase/analyze")
    @ResponseBody
    public Map<String, Object> analyzeTestCase(@RequestBody Map<String, String> request) throws JsonProcessingException {
        Map<String, Object> result = new HashMap<>();
        String testCode = request.get("testCode");
        String api=request.get("api");
        String environment=request.get("environment");
        String depedency=request.get("depedency");
        String testCaseResult = request.get("testCaseId");

        if (testCode == null) {
            result.put("success", false);
            result.put("message", "测试代码不能为空");
            return result;
        }


        String analysis = testCaseAnalysisService.generateAnalysisAndSuggestions(api,environment,depedency,testCode,testCaseResult);
        result.put("success", true);
        result.put("analysis", analysis);
        return result;
    }

    /**
     * 计算测试用例的覆盖度评分
     */
    @PostMapping("/api/testcase/calculate-coverage")
    @ResponseBody
    public Map<String, Object> calculateCoverage(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String testCode = request.get("testCode");

        if (testCode == null) {
            result.put("success", false);
            result.put("message", "测试代码不能为空");
            return result;
        }

        int coverageScore = testCaseAnalysisService.calculateCoverageScore(testCode);
        result.put("success", true);
        result.put("coverageScore", coverageScore);
        return result;
    }

    // ==================== 多种场景的执行接口 ====================

    /**
     * 执行单个测试用例,在补充建议里面需要用到测试结果评判执行情况
     * 暂时没做异常处理
     */
    @PostMapping("/api/execute/single")
    @ResponseBody
    public TestCaseResultDto executeSingleTestCase(@RequestBody Map<String, String> request) {
        StringBuilder results = new StringBuilder();
        String testCaseFilename = request.get("testCaseFileName");
        TestCaseResultDto testCaseResult =testExecutionService.testExecution(testCaseFilename);
        return testCaseResult;
        //获取名字
        //然后执行
        //以json格式存取
        //返回什么的




    }

    /**
     * 执行所有测试用例
     */
    @PostMapping("/api/execute/all")
    @ResponseBody
    public List<TestCaseResultDto> executeAllTestCases(@RequestBody List<Map<String, String>> request) {
        List<TestCaseResultDto> result = new ArrayList<>();
        List<String>testFilenames=new ArrayList<>();
        for (Map<String, String> requestMap : request) {
            testFilenames.add(requestMap.get("testCaseFileName"));
        }
        result=testExecutionService.testExecution(testFilenames);

        return result;

    }

    /**
     * 执行指定的测试用例列表
     */
    @PostMapping("/api/execute/batch")
    @ResponseBody
    public String executeBatchTestCases(@RequestBody Map<String, List<String>> request) {
        StringBuilder results = new StringBuilder();
        List<String> testCaseIds = request.get("testCaseIds");

        if (testCaseIds == null || testCaseIds.isEmpty()) {
            return "❌ 错误：测试用例ID列表不能为空！\n";
        }

        results.append("📋 执行指定的测试用例列表，共 " + testCaseIds.size() + " 个\n\n");

        for (String testCaseId : testCaseIds) {
            String testCode = testCaseManagementService.getTestCase(testCaseId);
            if (testCode != null) {
                results.append("🔹 执行测试用例：" + testCaseId + "\n");
                results.append(testExecutionService.executeGeneratedTests(testCode));
                results.append("\n");
            } else {
                results.append("❌ 测试用例 " + testCaseId + " 不存在！\n\n");
            }
        }

        return results.toString();
    }
}