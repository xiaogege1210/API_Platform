package com.example.demo.service.impl;

import com.example.demo.service.TestCaseNameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析指定格式的测试用例JSON文件，提取所有CaseName字段
 * 适配结构：PositiveCases[]/NegativeCases[] 下的每个对象的CaseName
 */
@Service
public class TestCaseNameParserImpl implements TestCaseNameParser {
    // 复用ObjectMapper，提升性能
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 核心方法：解析JSON文件，提取所有CaseName
     * @param jsonFilePath JSON文件路径（如："D:/test_cases.json"）
     * @return 去重、去空后的CaseName列表
     */
    @Override
    public List<String> extractAllCaseNames(String jsonFilePath) {
        List<String> caseNames = new ArrayList<>();

        try {
            // 1. 校验文件合法性
            File jsonFile = new File(jsonFilePath);
            if (!jsonFile.exists()) {
                throw new IllegalArgumentException("JSON文件不存在：" + jsonFilePath);
            }
            if (!jsonFile.isFile() || !jsonFilePath.endsWith(".json")) {
                throw new IllegalArgumentException("输入路径不是合法的JSON文件：" + jsonFilePath);
            }

            // 2. 读取并解析JSON文件
            JsonNode rootNode = OBJECT_MAPPER.readTree(jsonFile);

            // 3. 提取PositiveCases中的CaseName
            extractCaseNamesFromArray(rootNode, "PositiveCases", caseNames);
            // 4. 提取NegativeCases中的CaseName
            extractCaseNamesFromArray(rootNode, "NegativeCases", caseNames);

            // 5. 过滤空值、空白字符并去重（保证结果整洁）
            List<String> finalCaseNames = caseNames.stream()
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .distinct()
                    .toList();

            return finalCaseNames;

        } catch (Exception e) {
            throw new RuntimeException("解析测试用例JSON文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从指定名称的数组节点中提取CaseName
     * @param rootNode JSON根节点
     * @param arrayFieldName 数组字段名（PositiveCases/NegativeCases）
     * @param caseNames 存储结果的列表
     */
    private static void extractCaseNamesFromArray(JsonNode rootNode, String arrayFieldName, List<String> caseNames) {
        // 获取数组节点
        JsonNode arrayNode = rootNode.get(arrayFieldName);
        if (arrayNode == null || !arrayNode.isArray()) {
            System.out.println("警告：未找到" + arrayFieldName + "数组节点，跳过该部分解析");
            return;
        }

        // 遍历数组中的每个用例对象
        for (JsonNode caseNode : arrayNode) {
            // 提取CaseName字段（仅处理字符串类型）
            JsonNode caseNameNode = caseNode.get("CaseName");
            if (caseNameNode != null && caseNameNode.isTextual()) {
                caseNames.add(caseNameNode.asText().trim());
            }
        }
    }

//    // 测试示例：直接运行即可验证
//    public static void main(String[] args) {
//        // 替换为你的JSON文件实际路径
//        String jsonFilePath = "test_cases.json";
//
//        try {
//            List<String> allCaseNames = extractAllCaseNames(jsonFilePath);
//
//            // 打印结果
//            System.out.println("===== 解析出的所有CaseName =====");
//            for (int i = 0; i < allCaseNames.size(); i++) {
//                System.out.printf("%d. %s%n", i + 1, allCaseNames.get(i));
//            }
//            System.out.println("==============================");
//            System.out.println("总计提取到 " + allCaseNames.size() + " 个测试用例名称");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}