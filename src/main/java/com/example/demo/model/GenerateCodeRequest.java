package com.example.demo.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GenerateCodeRequest {
    private List<ApiTask> tasks;

    @Data
    public static class ApiTask {
        private String apiPath;
        private String method;
        private List<TestScenario> scenarios;
    }

    @Data
    public static class TestScenario {
        private String scenarioName;
        private Map<String, ParamValueInfo> paramValues;
        private String expectedStatusCode;
    }

    @Data
    public static class ParamValueInfo {
        private String value; // 用户填的值，可能为空
        private String type;  // 参数类型 (string, integer...)
    }
}