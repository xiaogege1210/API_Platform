package com.example.demo.service;

import com.example.demo.model.ApiEndpoint;
import com.example.demo.model.ApiParameter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 解析Swagger/OpenAPI文件
@Service
public class ParserService {

    public List<ApiEndpoint> parseSwaggerFile(MultipartFile file) throws IOException {
        // 1. 读取文件内容
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        // 2. 解析 Swagger/OpenAPI 内容
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(content);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            throw new RuntimeException("解析失败，无法识别的 Swagger/OpenAPI 格式。错误信息: " + result.getMessages());
        }
        
        // 存储解析出的API端点信息
        List<ApiEndpoint> endpoints = new ArrayList<>();

        // 3. 遍历 Path (URL)
        if (openAPI.getPaths() != null) {
            openAPI.getPaths().forEach((pathUrl, pathItem) -> {

                // 4. 遍历 Operation (GET/POST/PUT...)
                pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    ApiEndpoint endpoint = new ApiEndpoint();
                    endpoint.setPath(pathUrl);
                    endpoint.setMethod(httpMethod.name());
                    endpoint.setSummary(operation.getSummary());
                    endpoint.setParameters(new ArrayList<>());

                    // 5. 提取 Query/Path/Header 参数
                    if (operation.getParameters() != null) {
                        operation.getParameters().forEach(p -> {
                            ApiParameter param = new ApiParameter();
                            param.setName(p.getName());
                            param.setIn(p.getIn());
                            param.setRequired(Boolean.TRUE.equals(p.getRequired()));
                            if (p.getSchema() != null) {
                                param.setType(p.getSchema().getType());
                            }
                            endpoint.getParameters().add(param);
                        });
                    }

                    // 6. 提取 Request Body 参数
                    if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
                        io.swagger.v3.oas.models.media.MediaType jsonContent = operation.getRequestBody().getContent().get("application/json");
                        if (jsonContent != null && jsonContent.getSchema() != null) {
                            // 解析 Body 内部的字段
                            parseBodySchema(jsonContent.getSchema(), endpoint.getParameters());
                        }
                    }

                    endpoints.add(endpoint);
                });
            });
        }
        return endpoints;
    }

    // 解析 Body 内的字段
    private void parseBodySchema(Schema<?> schema, List<ApiParameter> paramList) {
        
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            properties.forEach((key, propSchema) -> {
                ApiParameter param = new ApiParameter();
                param.setName(key);
                param.setIn("body"); // 标记为 body 字段
                param.setType(propSchema.getType());
                param.setDescription("Body 字段");
                param.setRequired(false);
                paramList.add(param);
            });
        } else {
            ApiParameter param = new ApiParameter();
            param.setName("root_body");
            param.setIn("body");
            param.setType("object/json");
            param.setDescription("复杂结构 JSON");
            paramList.add(param);
        }
    }
}