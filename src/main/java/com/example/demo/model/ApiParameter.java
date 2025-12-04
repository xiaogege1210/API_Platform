package com.example.demo.model;

public class ApiParameter {
    private String name;        // 参数名
    private boolean required;   // 是否必填
    private String type;        // 参数类型 (string, integer, object)
    private String description; // 描述

    public ApiParameter(String name, boolean required, String type, String description) {
        this.name = name;
        this.required = required;
        this.type = type;
        this.description = description;
    }

    public String getName() { return name; }
    public boolean isRequired() { return required; }
    public String getType() { return type; }
    public String getDescription() { return description; }
}
