package com.example.demo.service.impl;

import com.example.demo.dto.TestCaseResultDto;
import com.example.demo.service.TestExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Java 1.8 适配的测试执行服务实现类
 * 核心功能：执行 resources/scripts 目录下的 Java 测试脚本（支持单个/批量执行）
 * 流程：文件校验 → 编译 → 执行 → 结果封装
 */

/**
 * todo:执行结果要不要也用文件保存下来
 * 每次销毁后自动删除，要不然会有重名问题，如果重名了怎么改
 */
@Service
public class TestExecutionServiceImpl implements TestExecutionService {
    // 日志实例（类级静态变量，Java 1.8 兼容）
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionServiceImpl.class);
    @Autowired
    private TestCaseAnalysisServiceImpl testCaseAnalysisServiceImpl;

    // 路径配置（自动适配 Windows/Linux 系统）
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String DEFAULT_OUTPUT_DIR = PROJECT_ROOT + File.separator + "target" + File.separator + "test-classes";
    private static final String SCRIPT_BASE_PATH = PROJECT_ROOT
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "scripts";

    // 编译输出目录（最终使用的目录，支持构造方法注入）
    private final String outputDir;
    /**
     * 默认构造方法（使用默认输出目录，Spring 自动注入时调用）
     */
    public TestExecutionServiceImpl() {
        this(DEFAULT_OUTPUT_DIR);
    }

    /**
     * 自定义输出目录构造方法（灵活配置场景使用）
     */
    public TestExecutionServiceImpl(String outputDir) {
        this.outputDir = outputDir;
        initOutputDir(); // 初始化输出目录
    }

    // ==================== 目录初始化 ====================
    /**
     * 初始化编译输出目录（不存在则创建）
     */
    private void initOutputDir() {
        logger.info("开始初始化编译输出目录...");
        File dir = new File(outputDir);
        if (!dir.exists()) {
            boolean isCreated = dir.mkdirs();
            if (isCreated) {
                logger.info("✅ 成功创建输出目录：{}", outputDir);
            } else {
                logger.error("❌ 创建输出目录失败：{}", outputDir);
                throw new RuntimeException("编译输出目录创建失败，无法执行后续编译流程");
            }
        } else {
            logger.info("ℹ️ 输出目录已存在：{}", outputDir);
        }
    }

    // ==================== 核心执行方法（批量执行）====================
    /**
     * 批量执行 Java 脚本（输入脚本文件名列表，如 ["Test1.java", "Test2.java"]）
     */
    @Override
    public List<TestCaseResultDto> testExecution(List<String> scriptNames) {
        logger.info("\n==================== 开始批量执行脚本 ====================");
        logger.info("待执行脚本数量：{}", scriptNames == null ? 0 : scriptNames.size());

        List<TestCaseResultDto> totalResults = new ArrayList<>();
        if (scriptNames == null || scriptNames.isEmpty()) {
            logger.warn("⚠️ 待执行脚本列表为空，直接返回空结果");
            return totalResults;
        }

        // 遍历每个脚本，执行并收集结果
        for (String scriptName : scriptNames) {
            logger.info("\n--------------------------------------------------");
            logger.info("开始执行脚本：{}", scriptName);
            // 执行单个脚本，将结果加入总列表
            TestCaseResultDto singleResult = testExecution(scriptName);
            totalResults.add(singleResult);
            logger.info("脚本执行结束：{}（状态：{}）", scriptName, singleResult.isPassed() ? "成功" : "失败");
        }

        logger.info("\n==================== 批量执行完成 ====================");
        logger.info("总执行脚本数：{}，成功数：{}，失败数：{}",
                totalResults.size(),
                totalResults.stream().filter(TestCaseResultDto::isPassed).count(),
                totalResults.stream().filter(result -> !result.isPassed()).count());
        return totalResults;
    }

    // ==================== 核心执行方法（单个执行）====================
    /**
     * 单个执行 Java 脚本（输入脚本文件名，如 "Test1.java"）
     */
    @Override
    public TestCaseResultDto testExecution(String scriptName) {
        // 初始化结果对象（默认失败，后续成功再修改）
        TestCaseResultDto resultDto = new TestCaseResultDto();
        resultDto.setTestCaseName(scriptName);
        resultDto.setPassed(false);
        resultDto.setExecuteTime(LocalDateTime.now()); // 记录执行时间

        try {
            // 1. 拼接完整文件路径
            String fullFilePath = SCRIPT_BASE_PATH + File.separator + scriptName;
            logger.info("脚本完整路径：{}", fullFilePath);

            // 2. 读取并校验文件
            File javaFile = readAndValidateMockTestFile(fullFilePath);
            if (javaFile == null) {
                resultDto.setFailureReason("文件校验失败（不存在、无权限或非Java文件）");

                return resultDto;
            }

            // 3. 编译 Java 文件
            boolean compileSuccess = compileJavaFile(javaFile);
            if (!compileSuccess) {

                resultDto.setFailureReason("Java 文件编译失败");
                String OriginScript=readFileContent(javaFile);

                String optScript=testCaseAnalysisServiceImpl.OptimizedScript(OriginScript,resultDto);
                System.out.println(optScript);
                resultDto.setOutputText(optScript);

                return resultDto;
            }

            // 4. 提取测试类名（文件名 = 类名，无包名场景）
            String testClassName = extractClassName(scriptName);
            logger.info("提取测试类名：{}", testClassName);
            //感觉不需要提取

            // 5. 执行测试类（获取方法级结果）
            List<TestCaseResultDto> methodResults = executeTestClass(testClassName);
            if (methodResults.isEmpty()) {
                resultDto.setFailureReason("测试类无有效测试方法");
                String OriginScript=readFileContent(javaFile);

                String optScript=testCaseAnalysisServiceImpl.OptimizedScript(OriginScript,resultDto);
                System.out.println(optScript);
                resultDto.setOutputText(optScript);
                return resultDto;
            }

            // 6. 汇总脚本级结果（只要有一个方法失败，脚本整体标记为失败）
            boolean allPassed = methodResults.stream().allMatch(TestCaseResultDto::isPassed);
            resultDto.setPassed(allPassed);
            if (allPassed) {
                resultDto.setFailureReason("");
                resultDto.setOutputText(String.format("脚本执行成功，共 %d 个测试方法全部通过", methodResults.size()));
            } else {
                // 收集所有失败方法的原因
                String failureReason = methodResults.stream()
                        .filter(result -> !result.isPassed())
                        .map(r -> r.getTestCaseName() + "：" + r.getFailureReason())
                        .collect(Collectors.joining("；"));
                resultDto.setFailureReason("部分方法执行失败：" + failureReason);
                resultDto.setOutputText(String.format("脚本执行完成，成功 %d 个方法，失败 %d 个方法",
                        methodResults.stream().filter(TestCaseResultDto::isPassed).count(),
                        methodResults.stream().filter(result -> !result.isPassed()).count()));
                String OriginScript=readFileContent(javaFile);

                String optScript=testCaseAnalysisServiceImpl.OptimizedScript(OriginScript,resultDto);
                System.out.println(optScript);
                resultDto.setOutputText(optScript);
            }

        } catch (Exception e) {
            logger.error("脚本执行异常：{}", e.getMessage(), e);
            resultDto.setFailureReason("脚本执行异常：" + e.getMessage());
        }

        return resultDto;
    }
    /**
     * 读取文件内容
     */
    private String readFileContent(File file) throws IOException {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        // 使用NIO Files读取，支持大文件
        Path filePath = file.toPath();
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }


    // ==================== 辅助方法：提取类名（文件名 -> 类名）====================
    /**
     * 从脚本文件名提取测试类名（如 "Test1.java" -> "Test1"）
     */
    /**
     * 从脚本路径中提取类名
     * 例如：hh/test.java -> test
     *      test.java -> test
     *      a/b/c/MyTest.java -> MyTest
     */
    private String extractClassName(String scriptName) {
        if (scriptName == null || !scriptName.endsWith(".java")) {
            return "";
        }
        // 去掉 .java 后缀
        String nameWithoutExt = scriptName.substring(0, scriptName.lastIndexOf("."));
        // 获取文件名（去掉路径部分）
        String fileName = nameWithoutExt;
        if (nameWithoutExt.contains(File.separator)) {
            fileName = nameWithoutExt.substring(nameWithoutExt.lastIndexOf(File.separator) + 1);
        } else if (nameWithoutExt.contains("/")) {
            // 兼容 Linux 路径分隔符
            fileName = nameWithoutExt.substring(nameWithoutExt.lastIndexOf("/") + 1);
        } else if (nameWithoutExt.contains("\\")) {
            // 兼容 Windows 路径分隔符
            fileName = nameWithoutExt.substring(nameWithoutExt.lastIndexOf("\\") + 1);
        }
        return fileName;
    }

    // ==================== 辅助方法：文件读取与校验 ====================
    /**
     * 读取并校验 Java 文件（存在性、可读性、文件类型）
     */
    private File readAndValidateMockTestFile(String fileName) {
        File file = new File(fileName);

        // 1. 校验文件是否存在
        if (!file.exists()) {
            logger.error("❌ 未找到文件：{}", fileName);
            return null;
        }

        // 2. 校验是否为文件（非目录）
        if (!file.isFile()) {
            logger.error("❌ 路径不是文件：{}", fileName);
            return null;
        }

        // 3. 校验文件是否可读
        if (!file.canRead()) {
            logger.error("❌ 文件无读取权限：{}", fileName);
            return null;
        }

        // 4. 校验是否为 Java 文件
        if (!fileName.endsWith(".java")) {
            logger.error("❌ 不是 Java 文件：{}", file.getName());
            return null;
        }

        // 5. 校验通过
        logger.info("✅ 文件校验通过：{}", fileName);
        return file;
    }

    // ==================== 核心方法：编译 Java 文件 ====================
    /**
     * 编译 Java 文件（调用 javac 命令）
     */
    public boolean compileJavaFile(File javaFile) throws IOException {
        logger.info("\n==================== 开始编译 ====================");
        logger.info("待编译文件：{}", javaFile.getAbsolutePath());

        // 1. 校验入参
        if (javaFile == null || !javaFile.exists() || !javaFile.isFile()) {
            logger.error("❌ 编译失败：无效的 Java 文件");
            return false;
        }

        // 2. 构建 classpath（兼容 Windows/Linux 路径分隔符）
        String pathSeparator = File.pathSeparator;
        String classpath = System.getProperty("java.class.path") + pathSeparator + outputDir;
        logger.info("编译 classpath：{}", classpath);

        // 3. 构建 javac 命令
        ProcessBuilder pb = new ProcessBuilder(
                "javac",
                "-encoding", StandardCharsets.UTF_8.name(), // 强制 UTF-8 编码
                "-cp", classpath,
                "-d", outputDir,
                "-g", // 生成调试信息
                javaFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true); // 合并错误流和标准流
        Process process = null;
        InputStream inputStream = null;

        try {
            // 启动编译进程
            process = pb.start();
            inputStream = process.getInputStream();

            // 读取编译日志
            String compileLog = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            boolean hasErrorLog = compileLog != null && !compileLog.trim().isEmpty();

            // 打印编译日志
            if (hasErrorLog) {
                logger.warn("编译日志：\n{}", compileLog);
            } else {
                logger.info("编译日志：无错误");
            }

            // 等待编译完成，获取退出码
            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("❌ 编译被中断", e);
                return false;
            }

            // 判断编译结果
            if (exitCode == 0) {
                logger.info("✅ 编译成功！class 文件输出至：{}", outputDir);
                return true;
            } else {
                logger.error("❌ 编译失败（退出码：{}）", exitCode);
                return false;
            }

        } finally {
            // 释放资源
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭编译流失败", e);
                }
            }
            if (process != null) {
                process.destroy();
            }
            logger.info("==================== 编译流程结束 ====================\n");
        }
    }

    // ==================== 核心方法：执行测试类（JUnit 5）====================
    /**
     * 执行已编译的测试类，返回方法级执行结果
     */
    private List<TestCaseResultDto> executeTestClass(String testClassName) {
        logger.info("\n==================== 开始执行测试类 ====================");
        logger.info("待执行测试类名：{}", testClassName);
        logger.info("class 文件查找路径：{}", outputDir);
        //直接放在默认路径，也就是最好不要重名

        List<TestCaseResultDto> results = new ArrayList<>();

        URLClassLoader classLoader = null;
        ClassLoader originalContextClassLoader = null;
        try {
            // 1. 创建自定义类加载器，将 outputDir 添加到 classpath
            // 使用系统类加载器作为父类，确保能访问所有 JUnit 依赖
            File outputDirFile = new File(outputDir);
            URL[] urls = new URL[]{outputDirFile.toURI().toURL()};
            classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

            // 2. 保存原始上下文类加载器，并设置为自定义类加载器
            // 这样 JUnit Platform 就能使用正确的类加载器来查找 TestEngine
            originalContextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            // 3. 使用自定义类加载器加载测试类
            Class<?> testClass = classLoader.loadClass(testClassName);
            logger.info("✅ 成功加载测试类：{}（类加载器：{}）",
                    testClass.getName(), testClass.getClassLoader().toString());

            // 2. 筛选带 @Test 注解的测试方法（Java 1.8 数组转 List）
            List<Method> testMethods = new ArrayList<>(Arrays.asList(testClass.getDeclaredMethods()));
            testMethods = testMethods.stream()
                    .filter(method -> method.isAnnotationPresent(Test.class))
                    .collect(Collectors.toList());

            if (testMethods.isEmpty()) {
                logger.warn("⚠️  测试类中无有效测试方法（未找到 @Test 注解）");
                return results;
            }

            // 打印找到的测试方法
            logger.info("✅ 找到 {} 个测试方法待执行", testMethods.size());
            for (Method method : testMethods) {
                logger.info("  - 测试方法：{}", method.getName());
            }

            // 3. 初始化 JUnit 5 启动器
            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener resultListener = new SummaryGeneratingListener();

            // 4. 配置执行请求
            LauncherDiscoveryRequest executeRequest = LauncherDiscoveryRequestBuilder
                    .request()
                    .selectors(DiscoverySelectors.selectClass(testClass))
                    .build();

            // 5. 执行测试
            launcher.registerTestExecutionListeners(resultListener);
            logger.info("🚀 开始执行测试用例...");
            launcher.execute(executeRequest);

            // 6. 解析执行结果
            TestExecutionSummary summary = resultListener.getSummary();
            logger.info("📊 测试执行统计：成功 {} 个，失败 {} 个，跳过 {} 个",
                    summary.getTestsSucceededCount(),
                    summary.getTestsFailedCount(),
                    summary.getTestsSkippedCount());

            // 7. 缓存失败用例（Java 1.8 兼容的 Map 收集）
            Map<String, String> failedCaseMap = summary.getFailures().stream()
                    .collect(Collectors.toMap(
                            failure -> extractMethodName(failure.getTestIdentifier().getDisplayName()),
                            this::buildFailureReason
                    ));

            // 8. 封装每个方法的结果
            for (Method method : testMethods) {
                String methodName = method.getName();
                TestCaseResultDto methodResult = new TestCaseResultDto();
                methodResult.setTestCaseName(methodName);
                methodResult.setExecuteTime(LocalDateTime.now());

                if (failedCaseMap.containsKey(methodName)) {
                    // 失败用例
                    methodResult.setPassed(false);
                    methodResult.setFailureReason(failedCaseMap.get(methodName));
                    methodResult.setOutputText("测试方法执行失败");
                } else if (summary.getTestsSkippedCount() > 0 && isMethodSkipped(method, summary)) {
                    // 跳过用例
                    methodResult.setPassed(false);
                    methodResult.setFailureReason("测试用例被跳过");
                    methodResult.setOutputText("测试方法被跳过");
                } else {
                    // 成功用例
                    methodResult.setPassed(true);
                    methodResult.setFailureReason("");
                    methodResult.setOutputText("测试方法执行成功");
                }
                results.add(methodResult);
            }

            // 9. 去重（失败结果覆盖成功结果）
            results = deduplicateTestResults(results);

        } catch (ClassNotFoundException e) {
            logger.error("❌ 测试类加载失败", e);
            TestCaseResultDto errorResult = new TestCaseResultDto();
            errorResult.setTestCaseName(testClassName);
            errorResult.setPassed(false);
            errorResult.setFailureReason("找不到测试类：" + e.getMessage());
            results.add(errorResult);
        } catch (NoClassDefFoundError e) {
            logger.error("❌ 依赖缺失", e);
            TestCaseResultDto errorResult = new TestCaseResultDto();
            errorResult.setTestCaseName(testClassName);
            errorResult.setPassed(false);
            errorResult.setFailureReason("依赖缺失：" + e.getMessage());
            results.add(errorResult);
        } catch (Exception e) {
            logger.error("❌ 测试执行异常", e);
            TestCaseResultDto errorResult = new TestCaseResultDto();
            errorResult.setTestCaseName(testClassName);
            errorResult.setPassed(false);
            errorResult.setFailureReason("执行异常：" + e.getMessage());
            results.add(errorResult);
        } finally {
            // 恢复原始上下文类加载器
            if (originalContextClassLoader != null) {
                Thread.currentThread().setContextClassLoader(originalContextClassLoader);
            }
            // 关闭类加载器（释放资源）
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    logger.warn("关闭类加载器失败", e);
                }
            }
            logger.info("==================================================\n");
        }

        return results;
    }

    // ==================== 辅助方法：提取方法名（兼容 @DisplayName）====================
    private String extractMethodName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "";
        }
        // 兼容格式："methodName()" 或 "自定义显示名 (methodName())"
        if (displayName.contains("(") && displayName.contains(")")) {
            int startIdx = displayName.lastIndexOf("(");
            int endIdx = displayName.lastIndexOf(")");
            return displayName.substring(0, startIdx).trim();
        }
        return displayName.trim();
    }

    // ==================== 辅助方法：构建失败原因 ====================
    private String buildFailureReason(TestExecutionSummary.Failure failure) {
        Throwable ex = failure.getException();
        // Java 1.8 字符串拼接（替代文本块）
        StringBuilder reason = new StringBuilder();
        reason.append("异常类型：").append(ex.getClass().getSimpleName()).append("\n");
        reason.append("异常消息：").append(ex.getMessage() == null ? "无详细消息" : ex.getMessage()).append("\n");
        reason.append("堆栈摘要：\n");
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
            reason.append("  ").append(stackTrace[i]).append("\n");
        }
        return reason.toString().trim();
    }

    // ==================== 辅助方法：判断方法是否被跳过 ====================
    private boolean isMethodSkipped(Method method, TestExecutionSummary summary) {
        // JUnit 1.8.2 兼容处理
        if (summary.getTestsSkippedCount() == 0) {
            return false;
        }
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            String failureMethodName = extractMethodName(failure.getTestIdentifier().getDisplayName());
            if (failureMethodName.equals(method.getName())) {
                return true;
            }
        }
        return false;
    }

    // ==================== 辅助方法：去重测试结果 ====================
    private List<TestCaseResultDto> deduplicateTestResults(List<TestCaseResultDto> results) {
        // Java 1.8 stream 去重：按方法名分组，保留失败结果
        return results.stream()
                .collect(Collectors.groupingBy(TestCaseResultDto::getTestCaseName))
                .values().stream()
                .map(group -> {
                    // 优先保留失败结果
                    for (TestCaseResultDto result : group) {
                        if (!result.isPassed()) {
                            return result;
                        }
                    }
                    // 无失败结果则保留第一个
                    return group.get(0);
                })
                .collect(Collectors.toList());
    }
}