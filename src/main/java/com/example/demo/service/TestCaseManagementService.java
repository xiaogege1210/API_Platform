package com.example.demo.service;

import com.example.demo.dto.TestCaseResultDto;
import java.util.List;

public interface TestCaseManagementService {

    /**
     * 读取总目录下所有测试用例脚本（仅文件名）
     * @return 脚本文件名列表
     */
    List<String> findTestCaseAll();

    /**
     * 根据指定目录查找该目录下的所有脚本（仅文件名）
     * @param filePath 目标目录路径（绝对路径或相对路径）
     * @return 该目录下的脚本文件名列表
     */
    List<String> findTestCaseByDir(String filePath);

    /**
     * 根据目录+文件名，读取脚本的具体内容
     * @param filePath 脚本所在目录路径
     * @return 脚本文件的文本内容
     */
    String readTestCaseContent(String filePath);

    /**
     * 删除单个脚本文件
     * @param filePath 脚本文件的完整路径（目录+文件名）
     */
    void deleteTestCaseFile(String filePath);

    /**
     * 删除某个场景目录下的所有脚本（保留目录本身）
     * @param sceneDir 场景目录路径
     */
    void deleteAllTestCasesInDir(String sceneDir);

    /**
     * 更新单个脚本内容（覆盖原文件）
     * @param filePath 脚本所在目录路径
     * @param newContent 新的脚本内容
     */
    void updateTestCaseContent(String filePath, String newContent);

    /**
     * 新增测试用例脚本
     * @param filePath 保存目录路径
     * @param content 脚本初始内容
     */
    void createTestCaseFile( String filePath, String content);
}
