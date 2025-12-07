package com.example.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class ApiGroup {
    private List<ApiEndpoint> apis;
    private String info;

    // 构造函数
    public ApiGroup(List<ApiEndpoint> apis, String info) {
        this.apis = apis;
        this.info = info;
    }

    // getter & setter
    public List<ApiEndpoint> getApis() { return apis; }
    public void setApis(List<ApiEndpoint> apis) { this.apis = apis; }
    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
}
