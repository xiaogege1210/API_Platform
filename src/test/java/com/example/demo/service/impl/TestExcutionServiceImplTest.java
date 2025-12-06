package com.example.demo.service.impl;

import com.example.demo.DemoApplication;
import com.example.demo.dto.TestCaseResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = DemoApplication.class)
public class TestExcutionServiceImplTest {
    @Autowired
    private TestExecutionServiceImpl testExecutionServiceImpl;
    /**测试单个执行脚本
     *
     */
    @Test
    public void testExecuteTestClass() {
        TestCaseResultDto testCaseResultDto = new TestCaseResultDto();
        testExecutionServiceImpl.testExecution("mocktest.java");
        Assert.notNull(testCaseResultDto, "测试执行结果不能为null");
        System.out.printf("测试执行成功！结果：%s%n", testCaseResultDto.getOutputText());


    }
    @Test
    public void testExecuteTestsMethod() {
        List<String> name=new ArrayList<>();
        name.add("mocktest.java");
        name.add("mocktest1.java");
        List<TestCaseResultDto> testCaseResultDto=testExecutionServiceImpl.testExecution(name);
        Assert.notNull(testCaseResultDto, "测试执行结果不能为null");


    }
}
