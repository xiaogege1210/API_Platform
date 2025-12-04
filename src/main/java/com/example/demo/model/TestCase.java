package com.example.demo.model;

import java.util.List;

/**
 * TestCase
 * -----------------------------------
 * 描述一个自动化测试用例，包括：
 * - caseId：测试 id
 * - 场景名称
 * - 接口信息
 * - 前置操作
 * - 期望结果
 */
public class TestCase {

    private String caseId;               // 用例ID，例如 case_user_get_001
    private String scenarioName;         // 场景名称
    private ApiInfo api;                 // 接口信息
    private List<String> preSteps;       // 前置准备
    private ExpectedResult expected;     // 期望结果

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public ApiInfo getApi() { return api; }
    public void setApi(ApiInfo api) { this.api = api; }

    public List<String> getPreSteps() { return preSteps; }
    public void setPreSteps(List<String> preSteps) { this.preSteps = preSteps; }

    public ExpectedResult getExpected() { return expected; }
    public void setExpected(ExpectedResult expected) { this.expected = expected; }
}
