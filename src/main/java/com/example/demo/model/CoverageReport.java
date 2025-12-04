package com.example.demo.model;

import java.util.List;

/**
 * CoverageReport
 * -----------------------------------
 * 用于统计场景覆盖度，以及哪些场景没有被生成或执行
 */
public class CoverageReport {

    private int totalCases;        // 应生成的总场景数
    private int testedCases;       // 已执行成功的用例数
    private double coverageScore;  // 覆盖率（tested / total）
    private List<String> missingScenarios;

    public int getTotalCases() { return totalCases; }
    public void setTotalCases(int totalCases) { this.totalCases = totalCases; }

    public int getTestedCases() { return testedCases; }
    public void setTestedCases(int testedCases) { this.testedCases = testedCases; }

    public double getCoverageScore() { return coverageScore; }
    public void setCoverageScore(double coverageScore) { this.coverageScore = coverageScore; }

    public List<String> getMissingScenarios() {
        return missingScenarios;
    }
    public void setMissingScenarios(List<String> missingScenarios) {
        this.missingScenarios = missingScenarios;
    }
}
