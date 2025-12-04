package com.example.demo.model;


/**
 * ApiInfo(用于执行HTTP测试调用）
 * -----------------------------------
 * 用于存储单个接口的基本信息，例如：
 * - 请求方法（GET、POST）
 * - URL
 * - 请求参数
 */


public class ApiInfo {

    private String method;   // GET / POST
    private String baseUrl;  // 例如 http://localhost:8080
    private String path;     // 例如 /user/getInfo

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}