package com.example.demo.model;

/**
 * ExpectedResult
 * -----------------------------------
 * 自动化平台用于描述“期望结果”
 * 例如期望返回 200 状态码、响应体包含某些字段
 */
public class ExpectedResult {
    private int status;
    private String expectedBodyContains;

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getExpectedBodyContains() { return expectedBodyContains; }
    public void setExpectedBodyContains(String expectedBodyContains) {
        this.expectedBodyContains = expectedBodyContains;
    }
}

