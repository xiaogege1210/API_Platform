package com.example.demo.model;

import lombok.Data;

@Data
public class AnalysisRequest {
    // testCode 作为 Map 接收
    private String testCode;  // 或者 Object testCode;

    // 注意：字段名要和前端一致，或者使用 @JsonProperty
    private String testText;
    
    // 新增字段
    private String apiDoc;      // 接口文档
    private String environment;  // 环境
    private String dependency;   // 接口依赖关系
}
