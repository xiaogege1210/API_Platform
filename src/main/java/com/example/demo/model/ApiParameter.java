package com.example.demo.model;

import lombok.Data;

@Data
public class ApiParameter {
    private String name;        // 参数名
    private String in;          // 参数位置 (query, path, header, body)
    private String type;        // 参数类型 (string, integer, object)
    private boolean required;   // 是否必填
    private String description; // 描述
}