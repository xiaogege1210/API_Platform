package com.example.demo.model;

import lombok.Data;
import java.util.List;

@Data
public class ApiEndpoint {
    private String path;        // 接口 URL
    private String method;      // HTTP 方法 (GET, POST)
    private String summary;     // 接口名称/描述
    private List<ApiParameter> parameters; // 参数列表
}