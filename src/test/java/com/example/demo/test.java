package org.autotestdemo;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class test{

    // 项目路径配置（不变）
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String OUTPUT_DIR = PROJECT_ROOT + File.separator + "target" + File.separator + "test-classes";
    private static final String MOCK_TEST_FILE_PATH = PROJECT_ROOT
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "mocktest.java";

    private final TestExecutor executor = new TestExecutor(OUTPUT_DIR);

    @Test
    void testReadCompileRunMockTest() throws IOException {
        // 1. 校验文件
        File mockTestFile = readAndValidateMockTestFile();
        assertNotNull(mockTestFile, "mocktest.java 文件校验失败");

        // 2. 编译文件
        boolean isCompiled = executor.compileJavaFile(mockTestFile);
        assertTrue(isCompiled, "mocktest.java 编译失败");

        // 3. 执行测试类
        List<TestRunResult> testResults = executor.executeTestClass("mocktest");
        assertNotNull(testResults, "测试结果为空");
        assertFalse(testResults.isEmpty(), "未找到测试用例");

        // 4. 打印报告
        printTestReport(testResults);

        // 5. 断言所有用例通过
        for (TestRunResult result : testResults) {
            assertTrue(result.isPassed(),
                    String.format("用例 [%s] 失败：%s", result.getTestCaseName(), result.getFailureReason()));
        }
    }

    // 读取并校验文件（不变）
    private File readAndValidateMockTestFile() {
        File file = new File(MOCK_TEST_FILE_PATH);

        if (!file.exists()) {
            System.err.println("❌ 未找到文件：" + MOCK_TEST_FILE_PATH);
            return null;
        }
        if (!file.isFile()) {
            System.err.println("❌ 路径不是文件：" + MOCK_TEST_FILE_PATH);
            return null;
        }
        if (!file.canRead()) {
            System.err.println("❌ 文件无读取权限：" + MOCK_TEST_FILE_PATH);
            return null;
        }
        if (!MOCK_TEST_FILE_PATH.endsWith(".java")) {
            System.err.println("❌ 不是 Java 文件：" + file.getName());
            return null;
        }

        System.out.println("✅ 文件校验通过：" + MOCK_TEST_FILE_PATH);
        return file;
    }

    // 打印测试报告（修复：用 Java 8 原生方法替代 StringUtils.isBlank()）
    private void printTestReport(List<TestRunResult> results) {
        System.out.println("\n==================== 测试报告 ====================");
        long passed = 0;
        for (TestRunResult res : results) {
            if (res.isPassed()) {
                passed++;
            }
        }
        long failed = results.size() - passed;
        System.out.printf("总用例数：%d | 成功：%d | 失败：%d%n", results.size(), passed, failed);

        if (failed > 0) {
            System.out.println("失败详情：");
            for (TestRunResult res : results) {
                if (!res.isPassed()) {
                    // 处理失败原因可能为 null 的情况
                    String reason = res.getFailureReason() == null ? "未知原因" : res.getFailureReason();
                    System.out.printf("  - %s：%s%n", res.getTestCaseName(), reason);
                }
            }
        }
        System.out.println("==================================================\n");
    }

    // 核心工具类：编译+执行（完全兼容 Java 8，无第三方依赖）
    static class TestExecutor {
        private final String outputDir;

        public TestExecutor(String outputDir) {
            this.outputDir = outputDir;
            initOutputDir();
        }

        // 初始化输出目录（不变）
        private void initOutputDir() {
            //读取文件
            File dir = new File(outputDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println(created ? "✅ 创建输出目录：" + outputDir : "⚠️ 创建目录失败：" + outputDir);
            } else {
                System.out.println("ℹ️ 输出目录已存在：" + outputDir);
            }
        }

        // 编译 Java 文件（修复：用 Java 8 原生方法实现 isBlank 逻辑）
        public boolean compileJavaFile(File javaFile) throws IOException {
            System.out.println("\n==================== 开始编译 ====================");
            String classpath = System.getProperty("java.class.path") + ";" + outputDir;

            ProcessBuilder pb = new ProcessBuilder(
                    "javac",
                    "-encoding", StandardCharsets.UTF_8.name(),
                    "-cp", classpath,
                    "-d", outputDir,
                    javaFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String log = readInputStream(process.getInputStream());
            // 🌟 修复：Java 8 原生实现 isBlank 逻辑（判断字符串是否为 null/空字符串/仅含空白字符）
            boolean logIsBlank = (log == null) || (log.trim().length() == 0);
            System.out.println("编译日志：" + (logIsBlank ? "无错误" : log));

            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ 编译被中断：" + e.getMessage());
                return false;
            }

            System.out.println(exitCode == 0 ? "✅ 编译成功" : "❌ 编译失败（退出码：" + exitCode + "）");
            System.out.println("==================================================\n");
            return exitCode == 0;
        }

        // 执行测试类（完全兼容 Java 8）
        public List<TestRunResult> executeTestClass(String testClassName) {
            System.out.println("\n==================== 开始执行测试 ====================");
            final List<TestRunResult> results = new ArrayList<>();

            try {
                // 1. 加载测试类
                Class<?> testClass = Class.forName(testClassName);
                System.out.println("✅ 加载测试类成功：" + testClass.getName());

                // 2. 初始化 JUnit 5 启动器
                Launcher launcher = LauncherFactory.create();
                SummaryGeneratingListener listener = new SummaryGeneratingListener();

                // 3. 配置执行请求
                LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                        .request()
                        .selectors(DiscoverySelectors.selectClass(testClass))
                        .build();

                // 4. 执行测试
                launcher.registerTestExecutionListeners(listener);
                launcher.execute(request);

                // 5. 解析测试结果
                TestExecutionSummary summary = listener.getSummary();

                // 遍历测试方法（Java 8 数组用传统 for 循环）
                Method[] methods = testClass.getDeclaredMethods();
                for (Method method : methods) {
                    // 检查是否为测试方法（有 @Test 注解）
                    if (method.isAnnotationPresent(Test.class)) {
                        results.add(new TestRunResult(method.getName(), true, ""));
                    }
                }

                // 处理失败用例
                for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                    String caseName = failure.getTestIdentifier().getDisplayName();
                    String failureReason = failure.getException().getMessage();
                    results.add(new TestRunResult(caseName, false, failureReason));
                }

                // 去重：失败用例覆盖成功用例
                List<TestRunResult> deduplicated = deduplicateTestResults(results);
                System.out.println("✅ 测试执行完成");
                return deduplicated;

            } catch (ClassNotFoundException e) {
                String err = "找不到测试类：" + testClassName;
                System.err.println("❌ 执行失败：" + err);
                results.add(new TestRunResult(testClassName, false, err));
            } catch (Exception e) {
                String err = "执行异常：" + e.getMessage();
                System.err.println("❌ 执行失败：" + err);
                results.add(new TestRunResult(testClassName, false, err));
            }

            System.out.println("==================================================\n");
            return results;
        }

        /**
         * 去重测试结果（Java 8 原生 for 循环实现）
         */
        private List<TestRunResult> deduplicateTestResults(List<TestRunResult> results) {
            List<TestRunResult> deduplicated = new ArrayList<>();
            for (TestRunResult result : results) {
                boolean exists = false;
                for (int i = 0; i < deduplicated.size(); i++) {
                    TestRunResult existing = deduplicated.get(i);
                    if (existing.getTestCaseName().equals(result.getTestCaseName())) {
                        exists = true;
                        // 失败用例覆盖成功用例
                        if (existing.isPassed() && !result.isPassed()) {
                            deduplicated.set(i, result);
                        }
                        break;
                    }
                }
                if (!exists) {
                    deduplicated.add(result);
                }
            }
            return deduplicated;
        }

        // 读取流（不变）
        private String readInputStream(InputStream is) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString().trim();
        }
    }

    // 测试结果封装类（不变）
    static class TestRunResult {
        private final String testCaseName;
        private final boolean passed;
        private final String failureReason;

        public TestRunResult(String testCaseName, boolean passed, String failureReason) {
            this.testCaseName = testCaseName;
            this.passed = passed;
            this.failureReason = passed ? "" : (failureReason == null ? "未知原因" : failureReason);
        }

        public String getTestCaseName() { return testCaseName; }
        public boolean isPassed() { return passed; }
        public String getFailureReason() { return failureReason; }
    }
}