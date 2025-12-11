package com.example.demo.model;

import com.example.demo.dto.TestCaseResultDto;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * RunReport - 一次执行的整体报告
 */
@Getter
public class RunReport {

    // Getter & Setter 方法
    private int totalCount;
    private int passedCount;
    private int failedCount;
    private double passRate;

    private CoverageReport coverage; // key=接口名称, value=覆盖度%
    private List<TestCaseResultDto> failedDetails; // 失败详情
    private List<String> suggestions; // 自动补充建议

    // 空参构造器
    public RunReport() {
    }

    // 全参构造器（可选，方便批量赋值）
    public RunReport(int totalCount, int passedCount, int failedCount, double passRate,
                     CoverageReport apiCoverage, List<TestCaseResultDto> failedDetails,
                     List<String> suggestions) {
        this.totalCount = totalCount;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
        this.passRate = passRate;
        this.coverage = apiCoverage;
        this.failedDetails = failedDetails;
        this.suggestions = suggestions;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public void setApiCoverage(CoverageReport apiCoverage) {
        this.coverage = apiCoverage;
    }

    public void setFailedDetails(List<TestCaseResultDto> failedDetails) {
        this.failedDetails = failedDetails;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

}

