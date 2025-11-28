package com.example.demo.service;

import com.example.demo.model.GenerateCodeRequest;
import com.example.demo.common.JavaDynamicCompiler; // 改为新包路径
import org.springframework.stereotype.Service;

@Service
public class TestExecutionService {
    private final JavaDynamicCompiler javaCompiler;
    
    // 构造函数
    public TestExecutionService() {
        this.javaCompiler = new JavaDynamicCompiler();
    }
    /**
     * 直接执行生成的测试代码
     * @param testCode 生成的测试代码字符串
     * @return 执行结果和日志
     */
    public String executeGeneratedTests(String testCode) {
        StringBuilder result = new StringBuilder();
        
        try {
            // 1. 记录测试代码信息
            int methodCount = countTestMethods(testCode);
            result.append("📊 测试代码概览：\n");
            result.append("   - 测试方法数量: " + methodCount + "\n");
            result.append("   - 代码总行数: " + testCode.split("\\n").length + "\n\n");
            
            // 2. 修复测试代码（添加必要的实现）
            result.append("🔧 正在修复测试代码...\n");
            String fixedCode = javaCompiler.fixTestCode(testCode);
            
            // 3. 动态编译并执行
            String executionResult = javaCompiler.compileAndExecuteTest(fixedCode);
            result.append(executionResult);
            
        } catch (Exception e) {
            result.append("❌ 执行测试时发生系统错误: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        
        return result.toString();
    }
    
    /**
     * 统计测试代码中的测试方法数量
     */
    private int countTestMethods(String code) {
        int count = 0;
        String[] lines = code.split("\\n");
        for (String line : lines) {
            if (line.trim().startsWith("@Test")) {
                count++;
            }
        }
        return count;
    }
}