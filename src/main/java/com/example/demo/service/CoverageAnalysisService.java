package com.example.demo.service;

import com.example.demo.model.CoverageReport;

import java.util.List;

public interface CoverageAnalysisService {
    CoverageReport generateCoverageReport(String apiDoc, List<String> generatedTestScenarios);

    CoverageReport generateCoverageReportWithFuzzyMatch(String apiDoc, List<String> generatedTestScenarios);

    CoverageReport AiGenerateCoverageReport(String apiDoc, List<String> generatedTestScenarios);
}
