package com.example.demo.service;
import com.example.demo.dto.TestCaseResultDto;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 测试执行服务接口（Java 1.8 兼容）
 */
public interface TestExecutionService {
    // 批量执行脚本（方法名首字母小写，符合 Java 命名规范）
    List<TestCaseResultDto> testExecution(List<String> scriptNames);

    List<TestCaseResultDto> testExecutionwithoutpro(List<String> scriptNames);

    // 单个执行脚本（方法名首字母小写，参数为 String，返回 TestCaseResultDto）
    TestCaseResultDto testExecution(String scriptName);

    TestCaseResultDto testExecutionwithoutpro(String scriptName);
}