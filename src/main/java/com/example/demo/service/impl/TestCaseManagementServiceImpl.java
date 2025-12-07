package com.example.demo.service.impl;

import com.example.demo.dto.TreeNodeDto;
import com.example.demo.service.TestCaseManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TestCaseManagementServiceImpl implements TestCaseManagementService {

    // 测试用例根目录（动态获取项目路径，跨系统兼容）
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String DEFAULT_OUTPUT_DIR = PROJECT_ROOT + File.separator + "target" + File.separator + "test-classes";
    private static final String SCRIPT_BASE_PATH = PROJECT_ROOT
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "scripts";

    /**
     * 核心方法：读取根目录下所有文件和文件夹，返回树形结构JSON字符串列表
     */
    @Override
    public List<String> findTestCaseAll() {
        List<TreeNodeDto> treeNodeList = buildTree(Paths.get(SCRIPT_BASE_PATH));
        return treeNodeList.stream()
                .map(this::treeNodeToJson)
                .collect(Collectors.toList());
    }

    /**
     * 递归构建树形结构
     * @param currentPath 当前目录路径
     * @return 当前目录下的所有子节点（文件+文件夹）
     */
    private List<TreeNodeDto> buildTree(Path currentPath) {
        List<TreeNodeDto> children = new ArrayList<>();

        try {
            Files.list(currentPath)
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        String fullPath = path.toAbsolutePath().toString();

                        if (Files.isDirectory(path)) {
                            TreeNodeDto dirNode = new TreeNodeDto(name, "dir", fullPath);
                            dirNode.setChildren(buildTree(path));
                            children.add(dirNode);
                        } else {
                            TreeNodeDto fileNode = new TreeNodeDto(name, "file", fullPath);
                            children.add(fileNode);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("构建文件树形结构失败：" + currentPath, e);
        }

        return children;
    }

    /**
     * 辅助方法：将TreeNodeDto转为简单JSON字符串（处理特殊字符转义）
     */
    private String treeNodeToJson(TreeNodeDto node) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"id\":\"").append(node.getId()).append("\",")
                .append("\"name\":\"").append(escapeJson(node.getName())).append("\",")
                .append("\"type\":\"").append(node.getType()).append("\",")
                .append("\"path\":\"").append(escapeJson(node.getPath())).append("\",")
                .append("\"children\":[");

        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            String childrenJson = node.getChildren().stream()
                    .map(this::treeNodeToJson)
                    .collect(Collectors.joining(","));
            sb.append(childrenJson);
        }

        sb.append("]}");
        return sb.toString();
    }

    /**
     * 辅助方法：JSON字符串转义（处理特殊字符）
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 根据目录查询测试用例（返回树形结构JSON字符串列表）
     */
    @Override
    public List<String> findTestCaseByDir(String filePath) {
        filePath = SCRIPT_BASE_PATH + File.separator + filePath;
        Assert.hasText(filePath, "目录路径不能为空");
        Path dirPath = Paths.get(filePath);
        Assert.isTrue(Files.isDirectory(dirPath), "指定路径不是有效目录：" + filePath);

        List<TreeNodeDto> treeNodeList = buildTree(dirPath);
        return treeNodeList.stream()
                .map(this::treeNodeToJson)
                .collect(Collectors.toList());
    }

    /**
     * 读取文件内容（UTF-8编码，避免中文乱码）
     */
    @Override
    public String readTestCaseContent(String filePath) {
        Assert.hasText(filePath, "完整文件路径不能为空");
        Path fileFullPath = Paths.get(SCRIPT_BASE_PATH + File.separator + filePath);

        Assert.isTrue(Files.exists(fileFullPath), "文件不存在：" + fileFullPath);
        Assert.isTrue(Files.isRegularFile(fileFullPath), "指定路径不是文件（可能是目录）：" + fileFullPath);

        try {
            byte[] fileBytes = Files.readAllBytes(fileFullPath);
            return new String(fileBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + fileFullPath, e);
        }
    }

    /**
     * 删除单个脚本文件
     */
    @Override
    public Boolean deleteTestCaseFile(String filePath) {
        Assert.hasText(filePath, "文件路径不能为空");
        Path fileFullPath = Paths.get(SCRIPT_BASE_PATH + File.separator + filePath);
        Assert.isTrue(Files.exists(fileFullPath) && Files.isRegularFile(fileFullPath),
                "文件不存在或不是普通文件：" + fileFullPath);

        try {
            Files.delete(fileFullPath);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败：" + fileFullPath, e);
        }
    }

    /**
     * 删除某个场景下的所有脚本（递归删除目录及子内容）
     */
    @Override
    public Boolean deleteAllTestCasesInDir(String sceneDir) {
        Assert.hasText(sceneDir, "场景目录不能为空");
        Path dirPath = Paths.get(SCRIPT_BASE_PATH + File.separator + sceneDir);
        Assert.isTrue(Files.isDirectory(dirPath), "指定路径不是有效目录：" + sceneDir);

        try {
            Files.walk(dirPath)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // 倒序：先删文件，再删目录
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("删除失败：" + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("遍历目录失败：" + sceneDir, e);
        }
        return true;
    }

    /**
     * 单文件更新（不存在则创建，存在则覆盖）- Java 8 兼容
     */
    @Override
    public Boolean updateTestCaseContent(String filePath, String newContent) {
        Assert.hasText(filePath, "目录路径不能为空");
        Assert.hasText(newContent, "文件内容不能为空");

        // 规范化文件名（解决中文乱码）
        String normalizedFilePath = normalizeFullFilePath(filePath);
        Path fileFullPath = Paths.get(normalizedFilePath);

        try {
            // 确保父目录存在
            Files.createDirectories(fileFullPath.getParent());
            // Java 8 兼容写法：Files.write
            Files.write(fileFullPath, newContent.getBytes(StandardCharsets.UTF_8));
            log.info("文件更新/创建成功：{}", fileFullPath);
        } catch (IOException e) {
            log.error("更新文件失败：{}", fileFullPath, e);
            throw new RuntimeException("更新文件失败：" + fileFullPath, e);
        }
        return true;
    }

    /**
     * 批量更新文件（不存在则创建，存在则覆盖）- Java 8 兼容
     */
    @Override
    public Boolean updateTestCaseContent(List<String> filePaths, List<String> newContents) {
        // 批量参数校验
        Assert.isTrue(!CollectionUtils.isEmpty(filePaths), "批量文件路径列表不能为空");
        Assert.isTrue(!CollectionUtils.isEmpty(newContents), "批量文件内容列表不能为空");
        Assert.isTrue(filePaths.size() == newContents.size(),
                "文件路径列表与内容列表长度不匹配：路径数=" + filePaths.size() + "，内容数=" + newContents.size());

        boolean allSuccess = true;

        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            String newContent = newContents.get(i);

            // 跳过空路径/空内容（Java 8 原生写法：替代 StringUtils.isBlank()）
            if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
                log.warn("跳过第{}个文件：路径为空", i + 1);
                allSuccess = false;
                continue;
            }
            if (Objects.isNull(newContent) || newContent.trim().isEmpty()) {
                log.warn("跳过第{}个文件：内容为空（原始路径={}）", i + 1, filePath);
                allSuccess = false;
                continue;
            }

            // 规范化文件名（解决中文乱码）
            String normalizedFilePath = normalizeFullFilePath(filePath);
            Path fileFullPath = Paths.get(normalizedFilePath);

            try {
                Files.createDirectories(fileFullPath.getParent());
                // Java 8 兼容写法：Files.write
                Files.write(fileFullPath, newContent.getBytes(StandardCharsets.UTF_8));
                log.info("批量更新成功：{}", fileFullPath);
            } catch (IOException e) {
                log.error("批量更新失败：第{}个文件（规范化路径={}）", i + 1, fileFullPath, e);
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * 创建脚本文件（文件已存在则失败）- Java 8 兼容
     */
    @Override
    public void createTestCaseFile(String filePath, String content) {
        // 校验核心参数
        Assert.hasText(filePath, "文件相对路径不能为空（含文件名，如：scene1/test.java）");
        Assert.hasText(content, "脚本内容不能为空");

        // 规范化文件名（解决中文乱码）
        String normalizedFilePath = normalizeFullFilePath(filePath);
        Path fileFullPath = Paths.get(normalizedFilePath);

        // 校验文件是否已存在
        if (Files.exists(fileFullPath)) {
            throw new RuntimeException("文件已存在，创建失败：" + fileFullPath);
        }

        try {
            // 确保父目录存在
            Files.createDirectories(fileFullPath.getParent());
            // Java 8 兼容写法：Files.write
            Files.write(fileFullPath, content.getBytes(StandardCharsets.UTF_8));
            log.info("文件创建成功：{}", fileFullPath);
        } catch (IOException e) {
            log.error("创建文件失败：{}", fileFullPath, e);
            throw new RuntimeException("创建文件失败：" + fileFullPath, e);
        }
    }

    // ---------------------- 工具方法 ----------------------
    /**
     * 工具方法：规范化完整文件路径（解决中文乱码、特殊字符问题）
     * @param originalFilePath 原始相对路径（如：case_generated/search_chats_api/测试 1.java）
     * @return 规范化后的完整路径（如：D:/project/.../scripts/case_generated/search_chats_api/Test_1.java）
     */
    private String normalizeFullFilePath(String originalFilePath) {
        if (originalFilePath == null || originalFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("原始文件路径不能为空");
        }

        File originalFile = new File(originalFilePath);
        String originalFileName = originalFile.getName(); // 原始文件名（如：测试 1.java）
        String parentDir = originalFile.getParent(); // 父目录（如：case_generated/search_chats_api）
        String normalizedFileName = normalizeFileName(originalFileName); // 规范化文件名（如：Test_1.java）

        // 构建规范化后的完整路径（跨系统兼容）
        Path normalizedPath;
        if (parentDir != null) {
            normalizedPath = Paths.get(SCRIPT_BASE_PATH).resolve(parentDir).resolve(normalizedFileName);
        } else {
            normalizedPath = Paths.get(SCRIPT_BASE_PATH).resolve(normalizedFileName);
        }

        return normalizedPath.toAbsolutePath().toString();
    }

    /**
     * 工具方法：规范化文件名（移除中文、特殊字符，符合 Java 命名规范）
     * @param originalFileName 原始文件名（如：测试 1.java）
     * @return 规范化文件名（如：Test_1.java）
     */
    private String normalizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return "TestDefault_" + System.currentTimeMillis() + ".java";
        }

        // 1. 移除中文和特殊字符（仅保留字母、数字、下划线、点）
        String normalized = originalFileName.replaceAll("[^a-zA-Z0-9_.]", "");
        // 2. 确保文件名不以数字/下划线开头（符合 Java 命名规范）
        if (normalized.length() > 0 && !Character.isLetter(normalized.charAt(0))) {
            normalized = "Test_" + normalized;
        }
        // 3. 确保文件后缀是 .java（如果是测试脚本）
        if (!normalized.endsWith(".java") && !normalized.contains(".")) {
            normalized += ".java";
        }
        // 4. 处理极端情况（规范化后为空）
        if (normalized.trim().isEmpty()) {
            normalized = "TestDefault_" + System.currentTimeMillis() + ".java";
        }

        return normalized;
    }
}