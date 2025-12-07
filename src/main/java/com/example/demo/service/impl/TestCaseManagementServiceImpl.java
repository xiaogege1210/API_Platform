package com.example.demo.service.impl;

import com.example.demo.dto.TreeNodeDto;
import com.example.demo.service.TestCaseManagementService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestCaseManagementServiceImpl implements TestCaseManagementService {

    // 测试用例根目录（固定为你指定的路径）
    //不是固定目录
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String DEFAULT_OUTPUT_DIR = PROJECT_ROOT + File.separator + "target" + File.separator + "test-classes";
    private static final String SCRIPT_BASE_PATH = PROJECT_ROOT
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "scripts";
    //private static final String TEST_CASE_ROOT_DIR = "D:\\IDEA2024\\project\\API_Platform\\src\\main\\resources\\scripts";

    /**
     * 核心方法：读取根目录下所有文件和文件夹，返回树形结构
     */
    @Override
    public List<String> findTestCaseAll() {
        // 实际返回的是树形DTO，这里为了兼容接口暂时转为JSON字符串（建议后续修改接口返回类型为List<TreeNodeDto>）
        //读取所有的文件，（文件及其文件夹）
        List<TreeNodeDto> treeNodeList = buildTree(Paths.get(SCRIPT_BASE_PATH));
        // 这里返回JSON格式字符串（如果前端需要直接解析，也可以修改接口返回类型为List<TreeNodeDto>）
        return treeNodeList.stream()
                .map(this::treeNodeToJson)
                .collect(Collectors.toList());
    }
    //返回json格式前端解析

    /**
     * 递归构建树形结构
     * @param currentPath 当前目录路径
     * @return 当前目录下的所有子节点（文件+文件夹）
     */
    private List<TreeNodeDto> buildTree(Path currentPath) {
        List<TreeNodeDto> children = new ArrayList<>();

        try {
            // 遍历当前目录下的所有文件/文件夹
            Files.list(currentPath)
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        String fullPath = path.toAbsolutePath().toString();

                        if (Files.isDirectory(path)) {
                            // 文件夹：递归构建子节点
                            TreeNodeDto dirNode = new TreeNodeDto(name, "dir", fullPath);
                            dirNode.setChildren(buildTree(path)); // 递归处理子目录
                            children.add(dirNode);
                        } else {
                            // 文件：直接创建文件节点（无children）
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
     * 辅助方法：将TreeNodeDto转为简单JSON字符串（也可以用Jackson序列化）
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

    // ---------------------- 其他接口方法实现 ----------------------
    @Override
    public List<String> findTestCaseByDir(String filePath) {
        //目录需要拼接
        filePath =SCRIPT_BASE_PATH + File.separator +filePath;
        Assert.hasText(filePath, "目录路径不能为空");
        Path dirPath = Paths.get(filePath);
        Assert.isTrue(Files.isDirectory(dirPath), "指定路径不是有效目录：" + filePath);

        // 读取指定目录下的文件/文件夹，返回树形结构JSON字符串
        List<TreeNodeDto> treeNodeList = buildTree(dirPath);
        return treeNodeList.stream()
                .map(this::treeNodeToJson)
                .collect(Collectors.toList());
    }

    /**
     * 读取文件，返回脚本内容
     * @param filePath
     * @return
     */
    @Override
    public String readTestCaseContent(String filePath) {
        // 1. 校验完整文件路径不能为空
        Assert.hasText(filePath, "完整文件路径不能为空");
        //

        // 2. 构建文件路径对象
        Path fileFullPath = Paths.get( SCRIPT_BASE_PATH+File.separator +filePath);


        // 3. 校验：文件必须存在且是普通文件（不是目录）
        Assert.isTrue(Files.exists(fileFullPath), "文件不存在：" + fileFullPath);
        Assert.isTrue(Files.isRegularFile(fileFullPath), "指定路径不是文件（可能是目录）：" + fileFullPath);

        // 4. 读取文件内容（UTF-8编码，避免中文乱码）
        try {
            return Files.readString(fileFullPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + fileFullPath, e);
        }
    }

    /**
     * 删除单个脚本
     * todo:场景下无任何脚本处理，自动删除目录
     * @param filePath 脚本文件的完整路径（目录+文件名）
     */
    @Override
    public Boolean deleteTestCaseFile(String filePath) {
        Assert.hasText(filePath, "文件路径不能为空");
        Path fileFullPath = Paths.get(SCRIPT_BASE_PATH+File.separator +filePath);
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
     * 删除某个场景下的所有脚本
     * @param sceneDir 场景目录路径
     */

    @Override
    public Boolean deleteAllTestCasesInDir(String sceneDir) {
        Assert.hasText(sceneDir, "场景目录不能为空");
        Path dirPath = Paths.get(SCRIPT_BASE_PATH+File.separator +sceneDir);
        Assert.isTrue(Files.isDirectory(dirPath), "指定路径不是有效目录：" + sceneDir);

        try {
            // 递归删除目录下所有文件和子目录
            Files.walk(dirPath)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // 倒序遍历（先删文件，再删目录）
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
     * 更新脚本
     *
     * @param filePath 脚本所在目录路径
     * @param newContent 新的脚本内容
     */
    @Override
    public Boolean updateTestCaseContent(String filePath, String newContent) {
        Assert.hasText(filePath, "目录路径不能为空");
        Assert.hasText(newContent, "文件内容不能为空");

        Path fileFullPath = Paths.get(SCRIPT_BASE_PATH+File.separator +filePath);
        try {
            // 确保父目录存在
            Files.createDirectories(fileFullPath.getParent());
            // 覆盖写入文件内容
            Files.writeString(fileFullPath, newContent);
        } catch (IOException e) {
            throw new RuntimeException("更新文件失败：" + fileFullPath, e);
        }
        return true;
    }



    /**
     * 创建脚本
     * 目录存在直接创建文件，目录不存在创建目录，文件已存在则创建不成功
     * @param filePath 相对保存路径（相对于 SCRIPT_BASE_PATH，含文件名，如：scene1/test1.py 或 test2.json）
     * @param content 脚本初始内容
     */
    @Override
    public void createTestCaseFile(String filePath, String content) {
        // 1. 校验核心参数（路径、内容都不能为空）
        Assert.hasText(filePath, "文件相对路径不能为空（含文件名，如：scene1/test.java）");
        Assert.hasText(content, "脚本内容不能为空");

        // 2. 构建完整文件路径（SCRIPT_BASE_PATH + 相对路径）
        // 用 Path.resolve 替代字符串拼接，自动适配系统路径分隔符（Windows\ / Linux/）
        Path fileFullPath = Paths.get(SCRIPT_BASE_PATH).resolve(filePath);

        // 3. 校验文件是否已存在（存在则直接抛出异常）
        if (Files.exists(fileFullPath)) {
            throw new RuntimeException("文件已存在，创建失败：" + fileFullPath);
        }

        try {
            // 4. 确保父目录存在（不存在则递归创建）
            Files.createDirectories(fileFullPath.getParent());
            // 5. 写入文件内容（指定 UTF-8 编码，避免中文乱码）
            Files.writeString(fileFullPath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("创建文件失败：" + fileFullPath, e);
        }
    }
}
