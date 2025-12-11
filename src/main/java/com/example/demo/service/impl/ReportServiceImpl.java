package com.example.demo.service.impl;

import com.example.demo.dto.TestCaseResultDto;
import com.example.demo.model.RunReport;
import com.example.demo.service.ReportService;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ReportServiceImpl implements ReportService {

    /**
     * 输入：一次执行的所有 TestCaseResultDto
     * 输出：RunReport（运行报告 + 场景覆盖度 + 建议）
     */
    @Override
    public RunReport generateReport(List<TestCaseResultDto> results) {

        RunReport report = new RunReport();
        report.setTotalCount(results.size());

        long passed = results.stream().filter(TestCaseResultDto::isPassed).count();
        long failed = results.size() - passed;

        report.setPassedCount((int) passed);
        report.setFailedCount((int) failed);
        report.setPassRate(results.size() == 0 ? 0 : passed * 1.0 / results.size());

        // 失败详情
        List<TestCaseResultDto> failedDetails = results.stream()
                .filter(r -> !r.isPassed())
                .collect(Collectors.toList());
        report.setFailedDetails(failedDetails);

        // ===============================
        //         自动补充建议
        // ===============================
        List<String> suggestions = new ArrayList<>();

        if (report.getPassRate() < 0.8) {
            suggestions.add("整体用例通过率偏低，建议检查最近的接口变更或参数规则。");
        }

        // 失败原因聚类简单分析
        Map<String, Long> failReasonStats = failedDetails.stream()
                .collect(Collectors.groupingBy(TestCaseResultDto::getFailureReason, Collectors.counting()));

        if (!failReasonStats.isEmpty()) {
            String topReason = failReasonStats.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
            suggestions.add("主要失败原因集中在：" + topReason + "，建议重点排查。");
        }

        report.setSuggestions(suggestions);

        return report;
    }
}
