package com.example.demo.controller;

import com.example.demo.dto.TestCaseResultDto;
import com.example.demo.model.*;
import com.example.demo.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final TestExecutionService testExecutionService;
    private final TestCaseAnalysisService testCaseAnalysisService;
    private final TestCaseManagementService testCaseManagementService;
    private final CoverageAnalysisService coverageAnalysisService;
    private final ParserService parserService;
    private final ExternalTestCaseGeneratorService externalTestCaseGeneratorService;

    @Autowired
    public ApiController(
            TestExecutionService testExecutionService,
            TestCaseAnalysisService testCaseAnalysisService,
            TestCaseManagementService testCaseManagementService,
            CoverageAnalysisService coverageAnalysisService,
            ParserService parserService,
            ExternalTestCaseGeneratorService externalTestCaseGeneratorService) {
        this.testExecutionService = testExecutionService;
        this.testCaseAnalysisService = testCaseAnalysisService;
        this.testCaseManagementService = testCaseManagementService;
        this.coverageAnalysisService = coverageAnalysisService;
        this.parserService = parserService;
        this.externalTestCaseGeneratorService = externalTestCaseGeneratorService;
    }

    // ==================== 解析API文档 ====================
    @PostMapping("/parse")
    @ResponseBody
    public ResponseEntity<?> parseSwaggerFile(@RequestParam("file") MultipartFile file) {
        try {
            List<ApiGroup> apiGroups = parserService.parseSwaggerFile(file);
            return ResponseEntity.ok(apiGroups);
        } catch (Exception e) {
            logger.error("解析Swagger文件失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("解析失败: " + e.getMessage()));
        }
    }

    // ==================== 生成测试代码 ====================
    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<?> generateTestCode(@RequestBody List<GenerateCodeRequest> request) {
        try {
            logger.info("开始生成测试代码，请求数量: {}", request.size());
            String generatedCode = externalTestCaseGeneratorService.generateTestCase(request);
            return ResponseEntity.ok(generatedCode);
        } catch (Exception e) {
            logger.error("生成测试代码失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("生成失败: " + e.getMessage()));
        }
    }

    // ==================== 测试用例管理 ====================

    @PostMapping("/testcase/save")
    @ResponseBody
    public ResponseEntity<?> saveTestCase(@RequestBody SaveTestCaseRequest request) {
        try {
            testCaseManagementService.updateTestCaseContent(request.getFilePath(), request.getContent());
            return ResponseEntity.ok(createSuccessResponse("测试用例保存成功"));
        } catch (Exception e) {
            logger.error("保存测试用例失败: {}", request.getFilePath(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("保存失败: " + e.getMessage()));
        }
    }

    @GetMapping("/testcase/getfile")
    @ResponseBody
    public ResponseEntity<?> getTestCase(@RequestParam("filepath") String filePath) {
        try {
            String testCode = testCaseManagementService.readTestCaseContent(filePath);
            if (testCode == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("测试用例不存在"));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("testCode", testCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取测试用例失败: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取失败: " + e.getMessage()));
        }
    }

    @GetMapping("/testcase/getdir")
    @ResponseBody
    public ResponseEntity<List<String>> getTestCaseDirectory(@RequestParam("filepath") String directoryPath) {
        try {
            List<String> testCases = testCaseManagementService.findTestCaseByDir(directoryPath);
            return ResponseEntity.ok(testCases);
        } catch (Exception e) {
            logger.error("获取目录测试用例失败: {}", directoryPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/testcase/all")
    @ResponseBody
    public ResponseEntity<List<String>> getAllTestCases() {
        try {
            List<String> allTestCases = testCaseManagementService.findTestCaseAll();
            return ResponseEntity.ok(allTestCases);
        } catch (Exception e) {
            logger.error("获取所有测试用例失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping("/testcase/update")
    @ResponseBody
    public ResponseEntity<?> updateTestCase(@RequestBody UpdateTestCaseRequest request) {
        try {
            testCaseManagementService.updateTestCaseContent(request.getFilePath(), request.getContent());
            return ResponseEntity.ok(createSuccessResponse("测试用例更新成功"));
        } catch (Exception e) {
            logger.error("更新测试用例失败: {}", request.getFilePath(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("更新失败: " + e.getMessage()));
        }
    }

    @PostMapping("/testcase/batch/update")
    @ResponseBody
    public ResponseEntity<?> batchUpdateTestCase(@RequestBody BatchUpdateRequest request) {
        try {
            boolean allSuccess = testCaseManagementService.updateTestCaseContent(
                    request.getFilePaths(),
                    request.getContents()
            );

            if (allSuccess) {
                return ResponseEntity.ok(createSuccessResponse("所有测试用例批量保存成功"));
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(createErrorResponse("部分测试用例保存失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("批量更新测试用例失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("批量更新失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/testcase/deletefile")
    @ResponseBody
    public ResponseEntity<?> deleteTestCase(@RequestParam("filepath") String filePath) {
        try {
            boolean success = testCaseManagementService.deleteTestCaseFile(filePath);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("测试用例删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("测试用例不存在"));
            }
        } catch (Exception e) {
            logger.error("删除测试用例失败: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("删除失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/testcase/deletedir")
    @ResponseBody
    public ResponseEntity<?> deleteTestCaseDirectory(@RequestParam("filepath") String directoryPath) {
        try {
            boolean success = testCaseManagementService.deleteAllTestCasesInDir(directoryPath);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("测试目录删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("目录不存在或为空"));
            }
        } catch (Exception e) {
            logger.error("删除测试目录失败: {}", directoryPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("删除失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/testcase/deletefiles")
    @ResponseBody
    public ResponseEntity<?> deleteTestCases(@RequestParam("filepath") List<String> filePaths) {
        try {
            if (filePaths == null || filePaths.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("文件路径列表不能为空"));
            }

            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (String filePath : filePaths) {
                try {
                    boolean success = testCaseManagementService.deleteTestCaseFile(filePath);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.add("文件不存在: " + filePath);
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("删除失败 " + filePath + ": " + e.getMessage());
                    logger.error("删除测试用例失败: {}", filePath, e);
                }
            }

            Map<String, Object> result = new HashMap<>();
            if (failCount == 0) {
                result.put("success", true);
                result.put("message", "所有测试用例删除成功");
            } else if (successCount > 0) {
                result.put("success", true);
                result.put("message", String.format("部分删除成功：成功 %d 个，失败 %d 个", successCount, failCount));
                result.put("errors", errorMessages);
            } else {
                result.put("success", false);
                result.put("message", "所有测试用例删除失败");
                result.put("errors", errorMessages);
            }
            result.put("successCount", successCount);
            result.put("failCount", failCount);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("批量删除测试用例失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("批量删除失败: " + e.getMessage()));
        }
    }

    // ==================== 测试用例分析 ====================

    @PostMapping("/testcase/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeTestCase(@RequestBody AnalysisRequest request) {
        try {
            if (request.getTestCode() == null || request.getTestCode().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("测试代码不能为空"));
            }

            String analysis = testCaseAnalysisService.generateAnalysisAndSuggestions(
                    request.getApiDoc(),
                    request.getEnvironment(),
                    request.getDependency(),
                    request.getTestCode(),
                    request.getTestText()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("analysis", analysis);
            return ResponseEntity.ok(result);
        } catch (JsonProcessingException e) {
            logger.error("JSON处理失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("JSON格式错误: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("分析测试用例失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("分析失败: " + e.getMessage()));
        }
    }

    @PostMapping("/testcase/calculate-coverage")
    @ResponseBody
    public ResponseEntity<?> calculateCoverage(
            @RequestParam("apiDoc") String apiDocContent,
            @RequestParam("testCases") List<String> testCaseNames) {

        try {
            if (apiDocContent == null || apiDocContent.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("API文档内容不能为空"));
            }

            if (testCaseNames == null || testCaseNames.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("测试用例列表不能为空"));
            }

            // 读取测试用例内容
            List<String> testCaseContents = new ArrayList<>();
            for (String testCaseName : testCaseNames) {
                String content = testCaseManagementService.readTestCaseContent(testCaseName);
                if (content != null) {
                    testCaseContents.add(content);
                } else {
                    logger.warn("测试用例不存在: {}", testCaseName);
                }
            }

            if (testCaseContents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("未找到有效的测试用例"));
            }

            CoverageReport report = coverageAnalysisService.generateCoverageReportWithFuzzyMatch(
                    apiDocContent,
                    testCaseContents
            );

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("计算覆盖度失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("计算失败: " + e.getMessage()));
        }
    }

    // ==================== 测试执行 ====================

    @PostMapping("/execute/single")
    @ResponseBody
    public ResponseEntity<?> executeSingleTestCase(@RequestBody ExecuteTestCaseRequest request) {
        try {
            TestCaseResultDto result = testExecutionService.testExecution(request.getTestCaseFileName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("执行单个测试用例失败: {}", request.getTestCaseFileName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("执行失败: " + e.getMessage()));
        }
    }

    @PostMapping("/execute/batch")
    @ResponseBody
    public ResponseEntity<?> executeBatchTestCases(@RequestBody List<ExecuteTestCaseRequest> requests) {
        try {
            List<String> fileNames = new ArrayList<>();
            for (ExecuteTestCaseRequest request : requests) {
                fileNames.add(request.getTestCaseFileName());
            }

            List<TestCaseResultDto> results = testExecutionService.testExecution(fileNames);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("批量执行测试用例失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("批量执行失败: " + e.getMessage()));
        }
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // ==================== 内部请求类 ====================

    // 内部静态类，用于封装请求参数
    public static class SaveTestCaseRequest {
        private String filePath;
        private String content;

        // getters and setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class UpdateTestCaseRequest {
        private String filePath;
        private String content;

        // getters and setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class BatchUpdateRequest {
        private List<String> filePaths;
        private List<String> contents;

        // getters and setters
        public List<String> getFilePaths() { return filePaths; }
        public void setFilePaths(List<String> filePaths) { this.filePaths = filePaths; }
        public List<String> getContents() { return contents; }
        public void setContents(List<String> contents) { this.contents = contents; }
    }

    public static class ExecuteTestCaseRequest {
        private String testCaseFileName;

        // getters and setters
        public String getTestCaseFileName() { return testCaseFileName; }
        public void setTestCaseFileName(String testCaseFileName) {
            this.testCaseFileName = testCaseFileName;
        }
    }
}