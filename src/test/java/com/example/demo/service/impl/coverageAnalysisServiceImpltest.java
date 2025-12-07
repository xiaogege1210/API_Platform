package com.example.demo.service.impl;

import com.example.demo.DemoApplication;
import com.example.demo.model.TestCase;
import com.example.demo.service.CoverageAnalysisService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;
@SpringBootTest(classes = DemoApplication.class)
public class coverageAnalysisServiceImpltest {
    @Autowired
    private CoverageAnalysisServiceImpl coverageAnalysisService;
    @Test
    public void generateCoverageReportWithFuzzyMatch() {
        String content = "This is a test content";
        String testCases = "This is a test cases";
        List<String> testCasesList = new LinkedList<>();
        testCasesList.add(testCases);
        System.out.println(coverageAnalysisService.generateCoverageReportWithFuzzyMatch(content, testCasesList).getCoverageScore());
    }
    //每次执行的时候都需要需要提示词
}
