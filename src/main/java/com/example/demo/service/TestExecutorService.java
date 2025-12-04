package com.example.demo.service;

import com.example.demo.model.TestRunResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * TestExecutorService
 * ------------------------------------
 * 功能：
 * 1. 使用 JUnit Platform Launcher 运行动态生成的 class
 * 2. 记录通过/失败的用例
 * 输入：测试类名（如 "com.example.demo.generated"）
 * 输出：List<TestRunResult>
 * 小白理解：
 * class 文件虽然编译好了，但你得把它“运行起来”
 * 这个类完成“执行 Java 测试类”这个工作。
 */
@Service
public class TestExecutorService {

    public List<TestRunResult> execute(String className) throws Exception {

        List<TestRunResult> results = new ArrayList<>();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(className))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();

        // 失败用例
        summary.getFailures().forEach(f -> {
            TestRunResult r = new TestRunResult();
            r.setTestCaseId(f.getTestIdentifier().getDisplayName());
            r.setStatus("FAILED");
            r.setError(f.getException().getMessage());
            results.add(r);
        });

        long successCount = summary.getTestsSucceededCount();
        for (int i = 0; i < successCount; i++) {
            TestRunResult r = new TestRunResult();
            r.setStatus("PASSED");
            results.add(r);
        }

        return results;
    }
}
