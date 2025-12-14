package com.example.demo.service;

import com.example.demo.model.CoverageReport;

import java.util.List;

public interface CoverageAnalysisService {

    //CoverageReport AiGenerateCoverageReport(String apiDoc,String extraScene);

    CoverageReport AiGenerateCoverageReport(String apiDoc, String extraScene, List<String> generatedTestScenarios);
}
