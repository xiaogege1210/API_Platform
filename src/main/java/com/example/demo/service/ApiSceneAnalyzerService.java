package com.example.demo.service;

import com.example.demo.model.ApiDoc;
import com.example.demo.model.ApiParameter;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiSceneAnalyzerService {

    /**
     * 自动分析接口文档 → 推断理论场景数
     */
    public List<String> analyze(ApiDoc doc) {

        List<String> scenes = new ArrayList<>();

        // ① 基础场景：正常调用
        scenes.add("正常调用");

        // ② Token 类场景
        if (doc.getParameters().stream().anyMatch(p -> p.getName().contains("token"))) {
            scenes.add("Token 缺失");
            scenes.add("Token 为空");
            scenes.add("Token 非法格式");
            scenes.add("Token 失效");
        }

        // ③ 必填参数场景
        for (ApiParameter param : doc.getParameters()) {
            if (param.isRequired()) {
                scenes.add(param.getName() + " 缺失");
                scenes.add(param.getName() + " 为空");
            }
        }

        // ④ 类型校验场景
        for (ApiParameter param : doc.getParameters()) {
            if (param.getType().equals("string")) {
                scenes.add(param.getName() + " 超长");
                scenes.add(param.getName() + " 非法字符");
            }
        }

        // ⑤ 错误码场景
        if (doc.getErrorCodes() != null) {
            for (String err : doc.getErrorCodes()) {
                scenes.add("错误码触发: " + err);
            }
        }

        return scenes;
    }
}
