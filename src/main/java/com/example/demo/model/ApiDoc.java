package com.example.demo.model;

import java.util.List;

/**
 * 用于自动解析文档、分析接口可能的场景；
 */

public class ApiDoc {
    private String apiName;
    private String method;
    private String path;
    private List<ApiParameter> parameters;
    private List<String> errorCodes;

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<ApiParameter> getParameters() { return parameters; }
    public void setParameters(List<ApiParameter> parameters) { this.parameters = parameters; }

    public List<String> getErrorCodes() { return errorCodes; }
    public void setErrorCodes(List<String> errorCodes) { this.errorCodes = errorCodes; }
}

