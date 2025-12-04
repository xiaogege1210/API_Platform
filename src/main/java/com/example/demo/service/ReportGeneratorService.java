package com.example.demo.service;

import com.example.demo.model.CoverageReport;
import com.example.demo.model.TestCase;
import com.example.demo.model.TestRunResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportGeneratorService
 * -------------------------------------
 * 功能：
 * 1. 计算场景覆盖度
 * 2. 找出哪些场景没有被执行
 * 输入：理论接口场景列表+测试用例列表
 * 输出：覆盖率报告对象
 */
@Service
public class ReportGeneratorService {

//    public CoverageReport generate(List<TestRunResult> executed, List<TestCase> allCases) {
//
//        CoverageReport report = new CoverageReport();
//        report.setTotalCases(allCases.size());
//        report.setTestedCases(executed.size());
//
//        report.setCoverageScore((double) executed.size() / allCases.size());
//
//        // 找遗漏场景
//        List<String> missing = allCases.stream()
//                .map(TestCase::getCaseId)
//                .filter(id -> executed.stream().noneMatch(r -> id.equals(r.getTestCaseId())))
//                .collect(Collectors.toList());
//
//        report.setMissingScenarios(missing);
//
//        return report;
//    }

    public CoverageReport calculate(List<String> docScenes, List<TestCase> generatedCases) {

        CoverageReport report = new CoverageReport();
        report.setTotalCases(docScenes.size());
        report.setTestedCases(generatedCases.size());

        double score = generatedCases.size() * 1.0 / docScenes.size();
        report.setCoverageScore(score);

        // 找出接口缺失场景
        report.setMissingScenarios(
                docScenes.stream()
                        .filter(s -> generatedCases.stream().noneMatch(t -> t.getScenarioName().equals(s)))
                        .toList()
        );

        return report;
    }

}
