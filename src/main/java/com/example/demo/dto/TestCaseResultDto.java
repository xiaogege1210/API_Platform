package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * 测试用例执行结果 DTO（Java 1.8 兼容）
 */
public class TestCaseResultDto {
    private String testCaseName; // 用例名称（脚本名/方法名）
    private boolean passed;      // 执行状态（成功/失败）
    private String outputText;   // 执行输出文本
    private String failureReason;// 失败原因
    private LocalDateTime executeTime; // 执行时间

    // Getter + Setter
    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
    }
}