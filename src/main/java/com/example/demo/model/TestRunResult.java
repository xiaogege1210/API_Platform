package com.example.demo.model;

/**
 * TestRunResult
 * -----------------------------------
 * 用于记录测试执行结果，例如
 * - 通过 / 失败
 * - 错误信息
 */
public class TestRunResult {

    private String testCaseId;
    private String status; // PASSED / FAILED
    private String error;  // 失败时的错误提示

    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
